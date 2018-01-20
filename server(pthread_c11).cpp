#include <cstdlib>
#include <iostream>
#include <cstdio>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <error.h>
#include <netdb.h>
#include <sys/epoll.h>
#include <unordered_set>
#include <signal.h>
#include <map>
#include <vector>
#include <sstream>
#include <algorithm>
#include <iterator>
#include <stdlib.h>
#include <ctime>
#include <thread>
#include <chrono>
#include <sys/time.h>
#include <cstring>
#define MAX_EVENTS 32

class PlayerEntry{
public:
    int status;
    float x;
    float y;

   PlayerEntry(int s);
   PlayerEntry(int s,float xp,float yp);
};
PlayerEntry::PlayerEntry(int s){
    status=s;
}
PlayerEntry::PlayerEntry(int s, float xp, float yp){
    status=s;
    x=xp;
    y=yp;
}

int init_map[10][10] ={
		{1, 1, 1, 1, 1, 1, 1, 1, 1, 1} ,
		{1, 0, 0, 0, 0, 0, 0, 0, 0, 1} ,
		{1, 0, 0, 0, 4, 0, 0, 0, 0, 1} ,
		{1, 0, 0, 0, 4, 0, 4, 0, 0, 1} ,
		{1, 0, 0, 0, 4, 0, 4, 0, 0, 1} ,
		{1, 0, 0, 0, 0, 0, 0, 0, 0, 1} ,
		{1, 0, 0, 0, 0, 0, 0, 0, 0, 1} ,
		{1, 0, 0, 0, 0, 0, 0, 0, 0, 1} ,
		{1, 0, 0, 0, 0, 0, 0, 0, 0, 1} ,
		{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
};
int game_map[10][10];

std::string players[4];
std::map<int, PlayerEntry*> Players;
bool start =false;
int epoll_fd;
struct epoll_event *events ;
struct epoll_event ev;
// server socket
int servFd;
int players_alive=0;

// client sockets
std::unordered_set<int> clientFds;

// handles SIGINT
void ctrl_c(int);

// sends data to clientFds excluding fd
void sendToAllBut(int fd, char * buffer, int count);

// converts cstring to port
uint16_t readPort(char * txt);

// sets SO_REUSEADDR
void setReuseAddr(int sock);

void acceptPlayer();

void reset_game(){
	std::cout<<"reset"<<std::endl;
	memcpy(game_map,init_map,sizeof(init_map));
	players_alive=0;
    Players.clear();

}

void bomb(int x, int y){

	game_map[x][y]=2;
	std::this_thread::sleep_for(std::chrono::milliseconds(3000));
	game_map[x][y]=0;

	int k=1;
	int l=1;
	int plr_coords[4][3];
	int j=0;
	for (std::pair<int, PlayerEntry*> plr : Players){
		int y_b= 9-static_cast<int>((plr.second->y+25)/72);
		int x_b= static_cast<int>((plr.second->x+25-280)/72);
		plr_coords[j][0]=plr.first;
		plr_coords[j][1]=y_b;
		plr_coords[j][2]=x_b;
		//std::cout<<"gracz "<<plr_coords[j][1]<<" "<<plr_coords[j][2]<<std::endl;
		j++;

	}

	for(int i=0;i<4;i++){

		if(i==0){
			k=0;
			l=0;
		}
		if(i==1){
			k=0;
			l=1;
		}
		if(i==2){
			k=-1;
			l=0;
		}
		if(i==3){
			k=0;
			l=-1;
		}
		while(game_map[x+k][y+l]!=1 && abs(k)<3 && abs(l)<3){

					for(int m=0;m<j;m++){
						//std::cout<<"gracz "<<plr_coords[j][1]<<" "<<plr_coords[j][2]<< " = "<<x+k <<" "<< y+l<<std::endl;
						if(plr_coords[m][1]==x+k && plr_coords[m][2]==y+l){
							auto it = Players.find(plr_coords[m][0]);
							it->second->status=3;
							std::cout<<"gracz "<<std::endl;
							players_alive-=1;
						}
					}
					if(game_map[x+k][y+l]==4){
						game_map[x+k][y+l]=3;
						break;
					}else{
						game_map[x+k][y+l]=3;
					}
					if(i%2==0){

						int ak= k<0 ? -1 : 1;
						k=k + ak;}
					else{
						int al =l<0 ? -1 : 1;
						l=l+ al;}
				}

	}

	std::this_thread::sleep_for(std::chrono::milliseconds(300));
	for(int i=0;i<4;i++){

			if(i==0){
				k=0;
				l=0;
			}
			if(i==1){
				k=0;
				l=1;
			}
			if(i==2){
				k=-1;
				l=0;
			}
			if(i==3){
				k=0;
				l=-1;
			}
			while(game_map[x+k][y+l]!=1 && abs(k)<3 && abs(l)<3){
						if(game_map[x+k][y+l]==3){
							game_map[x+k][y+l]=0;
						}
						if(i%2==0){
							int ak= k<0 ? -1 : 1;
							k=k + ak;}
						else{
							int al =l<0 ? -1 : 1;
							l=l+ al;}
					}

		}



}
//wysy³anie stanu gry do graczy
void printPl(){

	std::string players_string;

	bool first;
    while(true){
    	bool reset=false;
    	std::this_thread::sleep_for(std::chrono::milliseconds(40));
    	std::cout<<players_alive<<" "<<start<<" "<<std::endl;

    	if(players_alive==0 && start ){
    	   for (std::pair<int, PlayerEntry*> plr : Players){
    	    	plr.second->status=5;
    	    	}
    	    start=false;
    	    reset=true;
    	   }


    	if(players_alive==1 && start){
    		for (std::pair<int, PlayerEntry*> plr : Players){
    		    if(plr.second->status!=3)
    		    	plr.second->status=4;
    		    }
    		start=false;
    		reset=true;
    	}


    	//string ze stanem mapy
    	std::string map_string="";
    	for(int i=0;i<10;i++){
    	    for(int j=0;j<10;j++){
    	    	map_string=map_string+std::to_string(game_map[i][j]);

    	    	}

    	    }

    	bool ready_to_play=true;
    	first=true;
    	//string z danymi graczy
    	players_string="";

    	//sprawdzanie czy wszyscy gotowi
    	for (std::pair<int, PlayerEntry*> plr : Players){
    	    	if(plr.second->status==0)
    	    		ready_to_play=false;
    	    }

    	//jesli tak i jest iles graczy to start

    	if(ready_to_play && Players.size()>1 && !reset)
    		start=true;

    	//dodanie komunikaty ze start lub nie
        if(start)
        players_string="1";
        else
        players_string="0";

        //wpisanie danych kazdego gracza
        for (std::pair<int, PlayerEntry*> plr : Players){

			if(!first)
				players_string+=" ";//delmiter
			else
				first=false;
			players_string+=std::to_string(plr.first)+";"+std::to_string(plr.second->status)+";";//Fd;staus
			players_string+=std::to_string(plr.second->x)+","+std::to_string(plr.second->y);//x,y

    }
        for (std::pair<int, PlayerEntry*> plr : Players){
            if(plr.second->status==8){
            	std::cout<<"usunieto"<<plr.first<<std::endl;
            	Players.erase(plr.first);
            	players_alive-=1;
            	 }
            }


    players_string=map_string+players_string;
    //wys³anie do ka¿dego gracza
    for (std::pair<int, PlayerEntry*> plr : Players){
    	// std::cout<<players_string<<std::endl;
        int count =sizeof(char)*players_string.size();
        int w =write(plr.first,  players_string.c_str(), sizeof(char)*players_string.size());
       // std::cout<<players_string<<std::endl;

        if(w!=count){
        	if(start){
        		plr.second->status=8;
        	}else{
            Players.erase(plr.first);
            std::cout<<"usunieto"<<plr.first<<w<<std::endl;
            players_alive-=1;
        	}
        }
        if(reset)
		  reset_game();
        //std::cout<<"wyslano "<<plr.first<<"|"<<players_string<<std::endl;

    }


    }
}

//odebranie wiadomosci od klientow
void readb(){
    std::string s;
    char buffer[1024];
    bool ready_to_play;
    int count;


    while(true){



        int n = epoll_wait(epoll_fd, events, MAX_EVENTS,-1);


        if (n < 0) {
        perror ("epoll_wait");
        free (events);
        exit(0);
        }

        for (int i = 0; i < n; i++)
        {
       // printf ("event=%ld on fd=%dn",events[i].events,events[i].data.fd);
           // if( events[i].events == EPOLLIN && Players.count(events[i].data.fd)>0)
                count = read(events[i].data.fd, buffer, 1024);
                if(count>0){
                    s.assign(buffer,count);
                    //std::cout<<s<<std::endl;
                    std::istringstream iss(s);
                    std::vector<std::string> result(std::istream_iterator<std::string>{iss},std::istream_iterator<std::string>());
                    int plr_fd= events[i].data.fd;

                    //sprawdzenie czy pierwsza liczba to 2 (pod³o¿enia bomby)
                    if (result[0].find("2") != std::string::npos && start){

                          int coords[2];
                         int y_b= 9-static_cast<int>((strtof(result[2].c_str(),0)+25)/72);
                         int x_b= static_cast<int>((strtof(result[1].c_str(),0)+25-280)/72);
                         if(game_map[y_b][x_b]!=2){
                        	 std::cout<<"BOMBA "<<x_b<<" "<<y_b<<std::endl;
                        	 std::thread t(bomb,y_b,x_b);
                        	 t.detach();
                        	// game_map[y_b][x_b]=2;
                         }
                        	 //std::thread t(bomb,x_b,y_b);

                    }
                    auto it = Players.find(plr_fd);
                    //sprawdzenie czy pierwsza liczba to 1 (gotowoœæ w lobby)
                    if (result[0].find("1") != std::string::npos && !start){
                    	it->second->status=1;

                    }
                    //sprawdzenie czy pierwsza liczba to 0 (nie gotowoœæ w lobby)
                    if (result[0].find("0") != std::string::npos && !start){

                    	it->second->status=0;

                    }
                    //druga i trzeba liczba to po³o¿enie gracza

                      it->second->x=strtof(result[1].c_str(),0);
                      it->second->y=strtof(result[2].c_str(),0);


                }else{
                	epoll_event ev_del;
                	ev_del.events = EPOLLIN;
                	ev_del.data.fd = events[i].data.fd;
                	int ret = epoll_ctl (epoll_fd, EPOLL_CTL_DEL, events[i].data.fd, &ev_del);
                	if (ret)
                	perror ("epoll_ctl");

                }


        }

    }

}


int main(int argc, char ** argv){
	// get and validate port number
	//if(argc != 2) error(1, 0, "Need 1 arg (port)");
	//auto port = readPort(argv[1]);


        //Epoll
        epoll_fd=epoll_create1(0);
        events =  (epoll_event*) malloc (sizeof (struct epoll_event) * MAX_EVENTS);
        if (!events) {
        perror ("malloc");
        return 1;
        }

        auto port = "22222";

	// create socket
	servFd = socket(AF_INET, SOCK_STREAM, 0);
	if(servFd == -1) error(1, errno, "socket failed");

	// graceful ctrl+c exit
	//signal(SIGINT, ctrl_c);
	// prevent dead sockets from throwing pipe errors on write
	signal(SIGPIPE, SIG_IGN);

	setReuseAddr(servFd);

	// bind to any address and port provided in arguments
	sockaddr_in serverAddr;
        serverAddr.sin_family=AF_INET;

        serverAddr.sin_port=htons((short) strtoul(port, NULL, 0));
        serverAddr.sin_addr={INADDR_ANY};

	int res = bind(servFd, (sockaddr*) &serverAddr, sizeof(serverAddr));
	if(res) error(1, errno, "bind failed");

	// enter listening mode
	res = listen(servFd, 4);
	if(res) error(1, errno, "listen failed");
	reset_game();


        //watek1
	std::cout<<"start";
	std::thread t1,t2,t3;
        t1 = std::thread(acceptPlayer);
        t2 = std::thread(printPl);
        t3 = std::thread(readb);

        t1.join();
        t2.join();
        t3.join();

}



uint16_t readPort(char * txt){
	char * ptr;
	auto port = strtol(txt, &ptr, 10);
	if(*ptr!=0 || port<1 || (port>((1<<16)-1))) error(1,0,"illegal argument %s", txt);
	return port;
}

void setReuseAddr(int sock){
	const int one = 1;
	int res = setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &one, sizeof(one));
	if(res) error(1,errno, "setsockopt failed");
}

void ctrl_c(int){
	for (std::pair<int, PlayerEntry*> plr : Players)
		close(plr.first);
	close(servFd);

	printf("Closing server\n");
	exit(0);
}





void acceptPlayer(){
    while(true){
    	bool can_connect=true;
        sockaddr_in clientAddr{0};
        socklen_t clientAddrSize = sizeof(clientAddr);
        auto clientFd = accept(servFd, (sockaddr*) &clientAddr, &clientAddrSize);
        if(clientFd == -1) error(1, errno, "accept failed");

        auto id = std::to_string(clientFd);

        if(Players.size()>=4){
        	can_connect=false;
        	id="-1";
        }
        if(start){
                	can_connect=false;
                	id="-2";
                }
        ///Wys³anie numeru FD klientowi

        int w =write(clientFd,  id.c_str(), sizeof(char)*id.size());

        if(w<0){

             std::cout<<"B³¹d po³¹czenia z "<<clientFd<<std::endl;
        }
        else if(can_connect){
        linger lin;
        unsigned int y=sizeof(lin);
        lin.l_onoff=1;
        lin.l_linger=10;
        setsockopt(clientFd,SOL_SOCKET, SO_LINGER,(void*)(&lin), y);


        Players.insert(std::make_pair(clientFd, new PlayerEntry(0,350,80)));
        players_alive+=1;
        ev.events = EPOLLIN;
        ev.data.fd = clientFd;
        int ret = epoll_ctl (epoll_fd, EPOLL_CTL_ADD, clientFd, &ev);
        if (ret)
            perror ("epoll_ctl");


        //350,590 ; 860,590  ; 860,80 ; 350,80
        printf("new connection from: %s:%hu (fd: %d)\n", inet_ntoa(clientAddr.sin_addr), ntohs(clientAddr.sin_port), clientFd);
        }
    }
}
