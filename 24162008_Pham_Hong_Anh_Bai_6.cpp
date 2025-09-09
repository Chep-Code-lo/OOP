#include <iostream>
#include <limits>
#include <exception>
#include <stdexcept>
#include <string>
#include <cctype>
#include <vector>
#include <iomanip>
using namespace std;
struct student{
    string student_code, name, sex;
    double point;//Các thuộc tính trong struct
    void output() const{
        cout << student_code << " " << name << " " << sex << " " << point << "\n";
    }
};
struct student_rank{
    string student_code, name, sex;//Các thuộc tính trong struct
    double point;
    string rank;
    void output() const{
        cout << student_code << " " << name << " " << sex << " " << point << " " << rank << "\n";
    }
};
//Đọc và yêu cầu nhập lại số nguyên cho đến khi hợp lí
int read_choice(int min, int max){
	while(true){
		int choose;// Lưu giữ lựa chọn
        //Kiểm tra nếu nhập không phải số nguyên
		if(!(cin >> choose)){
			cin.clear();//Xóa trạng thái lỗi của cin
			cin.ignore(std :: numeric_limits<streamsize>::max(), '\n');//Bỏ hết phần còn lại trong dòng nhập
			cout << "Vui lòng nhập lại số! \n";
			continue;//quay lại vòng lặp để nhập lại
		}
        //Kiểm tra khoảng giới hạn
		if(choose < min || choose > max){
			cout << "Lựa chọn không hợp lệ ! Hãy nhập lại trong khoảng từ " << min << " đến " << max << "\n";
			continue; 
		}
		return choose;//Nhập đúng yêu cầu thì trả về giá trị và thoát hàm
	}
}
//Xóa màn hình console
void clear_screen(){
    #ifdef _WIN32
        system("cls"); // Hoạt động trên DOS/WINDOWS
    #else
        system("clear");//Hoạt động trên UNIX/LINUX/MACOS
    #endif
}
//Dừng màn hình chờ 
void pause_enter(){
    cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');//Bỏ hết phần còn lại của dòng nhập cũ
    cout << "Nhấn Enter để tiếp tục...";
    cin.get();//Dừng chương trình chờ gõ enter để tiếp tục
}
//Nhập thông tin sinh viên
void in(student &s, int id){
    cout << "Thông tin sinh viên thứ " << id + 1<< "\n";
    cout << "Nhập MSSV: ";
    cin >> s.student_code;
    cin.ignore(numeric_limits<streamsize>::max(), '\n'); // Bỏ Enter sau MSSV
    cout << "Nhập họ tên: ";
    getline(cin, s.name);//Sử dụng vì tên có khoảng cách giữa các từ
    //Nhập và kiểm tra giới tính đến khi hợp lệ thì ngừng
    while(true){
        cout << "Nhập giới tính (nam/nu/khac): ";
        getline(cin, s.sex);
        // đưa về chữ thường để so sánh
        for(char &c : s.sex) c = static_cast<unsigned char> (tolower(c));;
        //Check điều kiện phù hợp
        if(s.sex == "nam" || s.sex == "nu" || s.sex == "khac") 
            break;//Hợp lệ thì thoát vòng lặp
        cout << "Giới tính không hợp lệ! Vui lòng nhập lại.\n";
    }
    //Nhập và kiểm tra điểm số đến khi hợp lệ thì ngừng
    while(true){
        cout << "Nhập điểm (0 - 10): ";
        if(!(cin >> s.point)){
            cin.clear();//Xóa trạng thái lỗi của cin
            cin.ignore(numeric_limits<streamsize>::max(), '\n');//Bỏ hết phần còn lại trong dòng nhập
            cout << "Vui lòng nhập số!\n";
            continue;//Quay lại vòng lặp để nhập lại
        }
        //Check điều kiện phù hợp
        if(s.point < 0 || s.point > 10.0){
            cout << "Điểm phải nằm trong khoảng [0,10]. Nhập lại.\n";
            continue;//Quay lại vòng lặp để nhập lại
        }
        break;//Hợp lệ thì thoát ra khỏi vòng lặp
    }
}
//Nhập danh sách sinh viên
void input(vector<student> &ds){
    cout << "Nhập vào số lượng sinh viên";
    int n;
    while(!(cin >> n) || n <= 0){
        cin.clear();
        cin.ignore(std::numeric_limits<streamsize>::max(), '\n');
        cout << "Vui lòng nhập số nguyên dương!!";
    }
    cin.ignore(std::numeric_limits<streamsize>::max(), '\n');
    ds.clear();
    ds.reserve(n);
    for(int i=0; i<n; ++i){
        student temp;
        in(temp, i);
        ds.push_back(temp);
    }
}
//Chuẩn hóa tên sinh viên
string normalize_name(const string &name){
    string s = name;
    //Xóa bỏ khoảng trắng đầu
    while(!s.empty() && isspace(s.front())) s.erase(s.begin());
    //Xóa bỏ khoảng trắng cuối
    while(!s.empty() && isspace(s.back())) s.pop_back();
    //Đưa toàn bộ về chữ thường
    for(char &x : s) x = static_cast<unsigned char> (tolower(x));;
    //Xóa khoảng trắng thừa giữa các từ
    string temp, result;//Biến lưu giữ giá trị sau khi chuẩn hóa khoảng cách và biến hoàn thiện theo dạng đẹp
    bool space = false;//Đặt cờ đánh dấu khoảng cách
    for(char x : s){
        //Kiểm tra có khoảng cách không
        if(isspace(x)){
            if(!space){
                temp += ' ';//Chỉ lấy 1 khoảng cách
                space = true;
            }
        }
        else{
            temp += x;
            space = false;
        }
    }
    //Viết hoa chữ cái đầu
    bool flag = true;//Đặt cờ đánh dấu in hoa chữ cái đầu
    for(char x : temp){
        if(isspace(x)){
            result += x;
            flag = true;
        }
        else{
            if(flag){
                result += toupper(x);//In hoa chữ cái đầu lên
                flag = false;
            }
            else result += x;
        }
    }
    return result;
}
//Chuẩn hóa giới tính dạng đẹp
string normalize_sex(student &s){
    string tmp = s.sex;
    for(char &c : tmp) c = static_cast<unsigned char> (tolower(c));
    if(tmp == "nam")  return "Nam";
    if(tmp == "nu" || tmp == "nữ") return "Nữ";
    if(tmp == "khac" || tmp == "khác") return "Khác";
    return "Không xác định";
}
//In ra thông tin danh sách sinh viên dạng đẹp
void print(vector<student> &s){
    vector<student>add_list;
    for(size_t i=0; i<s.size(); ++i){
        student temp = s[i];
        add_list.push_back({temp.student_code, normalize_name(temp.name), normalize_sex(temp), temp.point});
    }
    for(size_t i=0; i<add_list.size(); ++i)
        add_list[i].output();
}
//Kiểm tra sinh viên có qua môn hay không
bool check_pass(student &s){
    if(s.point >= 5.0)
        return true;
    else 
        return false;
}
//In ra thông tin danh sách sinh viên đã qua môn
int count_pass; //Biến toàn cục để đếm số lượng sinh viên qua môn
vector<student>add_pass;//Biến toàn cục lưu danh sách qua môn
void check_pass(vector<student> &s){
    for(size_t i = 0; i < s.size(); ++i){
        student temp = s[i];
        if(check_pass(temp)){
            count_pass++;
            add_pass.push_back({temp.student_code, normalize_name(temp.name), normalize_sex(temp), temp.point});
        }
    }
}
void print_pass(vector<student> &s){
    check_pass(s);
    for(const auto &x : add_pass)   x.output();
}
//Lập điều kiện để xếp loại sinh viên
string rank_student(student &s){
    if(s.point < 4)
        return "Sinh viên này xếp loại kém!!!!!";
    else if(s.point >= 4 && s.point < 5)
        return "Sinh viên này xếp loại yếu!!!!";
    else if(s.point >= 5 && s.point < 7)
        return "Sinh viên này xếp loại trung bình!!!";
    else if(s.point >= 7 && s.point < 8)
        return "Sinh viên này xếp loại khá!!";
    else if(s.point >= 8 && s.point < 9)
        return "Sinh viên này xếp loại giỏi!";
    else if(s.point >= 9 && s.point <= 10.0)
        return "Sinh viên này xếp loại xuất sắc";
    return "Điểm không hợp lệ!!!";
}
//In ra bảng xếp loại sinh viên
vector<student_rank> add_rank;
void make_rank(vector<student> &s){
    for(size_t i = 0; i < s.size(); ++i){
        student temp = s[i];
        add_rank.push_back({temp.student_code, normalize_name(temp.name), normalize_sex(temp), temp.point, rank_student(temp)});
    }
}
void print_rank(vector<student> &s){
    make_rank(s);
    for(size_t i=0; i<add_rank.size(); ++i)
        add_rank[i].output();
}
//In ra điểm trung bình của các sinh viên
void average_core(vector<student> &s){
    double result = 0.0;
    for(size_t i=0; i<s.size(); ++i)
        result = result + s[i].point;
    cout << result/s.size();
}
//In ra sinh viên có điểm lớn nhất
void student_point_max(vector<student> &s){
     int max_temp = -1e9;
     for(size_t i=0; i<s.size(); ++i){
        if(max_temp < s[i].point)
            max_temp = s[i].point;
    }
    for(size_t i=0; i<s.size(); ++i)
        if(max_temp == s[i].point){
            student result = s[i];
            cout << normalize_name(result.name);
            break;
        }
}
//In ra thông tin có sinh viên nữ đạt loại giỏi không
void student_girl_point_good(vector<student> &s){
    make_rank(s);
    for(size_t i=0; i<add_rank.size(); ++i){
        if(add_rank[i].sex == "Nữ" && add_rank[i].rank == "Sinh viên này xếp loại giỏi!"){
            cout << "Có sinh viên nữ đạt loại giỏi";
            break;
        }
    }
    cout << "Không có sinh viên nữ đạt loại giỏi";
}
int main(){
    vector<student> s;
    bool has_input = false;
    while(true){
        clear_screen(); //Xóa màn hình trước khi in menu mới
        //Xây dựng màn hình menu
    	cout << "\n==== MENU ====\n";
    	cout << "1) Nhập danh sách sinh viên \n";
    	cout << "2) Xuất danh sách sinh viên \n";
    	cout << "3) In thông tin những sinh viên đã qua môn \n";
    	cout << "4) In bảng xếp loại sinh viên \n";
    	cout << "5) Cho biết tỉ lệ sinh viên qua môn \n";
        cout << "6) Tính điểm trung bình các sinh viên \n";
        cout << "7) Cho biết tên của sinh viên có điểm cao nhất \n";
        cout << "8) Cho biết sinh viên nào là nữ và đạt loại giỏi không? \n";
    	cout << "0) thoát chương trình \n";
    	cout << "Chọn: ";
    	int choose = read_choice(0, 9);//Biến lựa chọn 
        //Xử dụng cấu trúc try để xử lý các thao tác để dễ xử lý lỗi
        try{
            //Kiểm tra và xử lý thao tác
            if(choose == 0){
                cout << "Tạm biệt! \n";
                break;
            }
            //Kiểm tra và xử lý thao tác
            else if(choose == 1){
                input(s);//Nhập dữ liệu
                has_input = true;//Đánh dấu đã thêm dữ liệu
                pause_enter();//Dừng tạm thời
            }
            //Kiểm tra và xử lý thao tác
            else if(choose == 2){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << "Thông tin sinh viên hiện tại: \n"; print(s);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra và xử lý thao tác
            else if(choose == 3){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
                cout << "Thông tin những sinh viên qua môn: \n";
    			print_pass(s);//In ra giá trị thao tácc
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra và xử lý thao tác
            else if(choose == 4){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
                cout << "Bảng xếp loại sinh viên: \n";
    			print_rank(s);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra và xử lý thao tác
            else if(choose == 5){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			check_pass(s);
                double rate = s.empty()? 0.0 : (double)count_pass / (double)s.size() * 100.0;//Tính điểm trung bình
                cout << "Tỉ lệ sinh viên qua môn: " << fixed << setprecision(2) << rate << "\n";//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra và xử lý thao tác
            else if(choose == 6){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
                cout << "Điểm trung bình của các sinh viên là: "; average_core(s);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra và xử lý thao tác
            else if(choose == 7){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
                cout << "Tên sinh viên có điểm cao nhất là: "; student_point_max(s);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra và xử lý thao tác
            else if(choose == 8){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    		    student_girl_point_good(s);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
        }
        //Thông báo các lỗi được lấy ra từ cấu trúc try
        catch(const exception &e){
                cout << "Lỗi: " << e.what() << endl;//In ra lỗi cụ thể
                pause_enter();//Tạm dừng để nhìn lỗi
    	} 
    }
}