#include <iostream>
#include <limits>
#include <exception>
#include <stdexcept>
#include <string>
using namespace std;
struct student{
    string student_code, name, sex;
    double point;//Các thuộc tính trong struct
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
void input(student &s){
    cout << "Nhập MSSV: ";
    cin >> s.student_code;
    cin.ignore(numeric_limits<streamsize>::max(), '\n'); // Bỏ Enter sau MSSV
    cout << "Nhập họ tên: ";
    getline(cin, s.name);//Sử dụng vì tên có khoảng cách giữa các từ
    //Nhập và kiểm tra giới tính đến khi hợp lệ thì ngừng
    while(true){
        cout << "Nhập giới tính (nam/nữ/khác): ";
        getline(cin, s.sex);
        // đưa về chữ thường để so sánh
        for(char &c : s.sex) c = tolower(c);
        //Check điều kiện phù hợp
        if(s.sex == "nam" || s.sex == "nữ" || s.sex == "nu" || s.sex == "khác" || s.sex == "khac") 
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
        if(s.point < 0 || s.point > 10){
            cout << "Điểm phải nằm trong khoảng [0,10]. Nhập lại.\n";
            continue;//Quay lại vòng lặp để nhập lại
        }
        break;//Hợp lệ thì thoát ra khỏi vòng lặp
    }
}
// Xuất ra thông tin sinh viên
void print(student &s){
    cout << s.student_code << " " << s.name << " " << s.sex << " " << s.point << "\n";
}
//Kiểm tra và trả ra kết quả sinh viên có qua môn hay không
void check_pass(student &s){
    if(s.point >= 5)
        cout << "Sinh viên này qua môn!\n";
    else 
        cout << "Sinh viên này không qua môn!!!\n";
}
//Kiểm tra và đưa ra xếp loại sinh viên
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
//Chuẩn hóa tên sinh viên
void normalize_name(student &s){
    //Xóa bỏ khoảng trắng đầu
    while(!s.name.empty() && isspace(s.name.front())) s.name.erase(s.name.begin());
    //Xóa bỏ khoảng trắng cuối
    while(!s.name.empty() && isspace(s.name.back())) s.name.pop_back();
    //Đưa toàn bộ về chữ thường
    for(char &x : s.name) x = tolower(x);
    //Xóa khoảng trắng thừa giữa các từ
    string temp, result;//Biến lưu giữ giá trị sau khi chuẩn hóa khoảng cách và biến hoàn thiện theo dạng đẹp
    bool space = false;//Đặt cờ đánh dấu khoảng cách
    for(char x : s.name){
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
    cout << "Tên sau khi chuẩn hóa là: " << result << "\n";
}
int main(){
    student s;
    bool has_input = false;
    while(true){
        clear_screen(); //Xóa màn hình trước khi in menu mới
        //Xây dựng màn hình menu
    	cout << "\n==== MENU ====\n";
    	cout << "1) Nhập sinh viên \n";
    	cout << "2) Xuất sinh viên \n";
    	cout << "3) Kiểm tra sinh viên có qua môn hay không \n";
    	cout << "4) Cho biết xếp loại sinh viên \n";
    	cout << "5) Chuẩn hóa tên sinh viên \n";
    	cout << "0) thoát chương trình \n";
    	cout << "Chọn: ";
    	int choose = read_choice(0, 5);//Biến lựa chọn 
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
            else if(choose == 3){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			check_pass(s);//In ra giá trị thao tácc
                pause_enter();//Dừng tạm thời
    		}
            else if(choose == 4){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << rank_student(s) << "\n";//In ra giá trị thao tácc
                pause_enter();//Dừng tạm thời
    		}
            else if(choose == 5){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			normalize_name(s);//In ra giá trị thao tácc
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