#include <iostream>
#include <math.h>
#include <exception>
#include <stdexcept>
#include <limits>
using namespace std;
// Định nghĩa kiểu dữ liệu mới
struct point{
    double x, y;
};
//Tính khoảng cách giữa hai điểm
double distance(point &a, point &b){
    return sqrt((a.x - b.x)*(a.x -b.x) + (a.y - b.y)*(a.y - b.y));
}
//Tìm điểm đối xứng qua gốc tọa độ
point reflection_origin(const point &d){
    return {-d.x, -d.y};
}
//Tìm điểm đối xứng qua Ox
point reflection_x_axis(const point &d){
    return {d.x, -d.y};
}
//Tìm điểm đối xứng qua Oy
point reflection_y_axis(const point &d){
    return {-d.x, d.y};
}
//Nhập tọa độ 
void input(point &d, const string &name){
    cout << "Nhập vào tọa độ điểm " << name;
    cin >> d.x >> d.y;
}
//Xuất tọa độ
void print(const point &d){
    cout << d.x << "; " << d.y;
}
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
    cout << "\nNhấn Enter để tiếp tục...";
    cin.get();//Dừng chương trình chờ gõ enter để tiếp tục
}
int main(){
    point first;
    bool has_input = false; // Đặt cờ cho input
    while(true){
        clear_screen();//Xóa màn hình trước khi in menu mới
        //Xây dựng màn hình menu
    	cout << "\n==== MENU ====\n";
    	cout << "1) Nhập điểm \n";
    	cout << "2) Xuất điểm \n";
    	cout << "3) Tính khoảng cách giữa hai điểm \n";
    	cout << "4) Tìm điểm đối xứng qua gốc tọa độ \n";
    	cout << "5) Tìm điểm đối xứng qua trục Ox \n";
    	cout << "6) Tìm điểm đối xứng qua trục Oy\n";
    	cout << "0) thoát chương trình \n";
    	cout << "Chọn: ";
    	int choose = read_choice(0, 6);//Biến lựa chọn 
        //Xử dụng cấu trúc try để xử lý các thao tác để dễ xử lý lỗi
    	try{
            //Kiểm tra thao tác và xử lý
    		if(choose == 0){
    			cout << "Tạm biệt !\n";
    			break;
    		}
            //Kiểm tra thao tác và xử lý
    		else if(choose == 1){
    			input(first, "là: ");//Thao tác nhập tọa độ điểm
    			has_input = true;//Đánh dấu đã được thêm input
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra thao tác và xử lý
    		else if(choose == 2){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có tọa độ điểm được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << "Tọa độ hiện tại:"; 
                print(first);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra thao tác và xử lý
            else if(choose == 3){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có tọa độ được nhập! Hãy chọn 1 để nhập!\n";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			point second;//Khai báo kiểu dữ liệu tọa độ thứ 2
    			cout << "Nhập thêm tọa độ thứ 2 để tiếp tục! \n";
                input(second, "thứ hai là: ");//Gọi hàm để nhập vào tọa độ thứ 2
                cout << "Khoảng cách giữa hai điểm là: " << distance(first, second) << "\n";//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
            }
            //Kiểm tra thao tác và xử lý
            else if(choose == 4){
                cout << "Điểm đối xứng qua gốc tọa độ là: "; 
                print(reflection_origin(first));//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
            }
            //Kiểm tra thao tác và xử lý
            else if(choose == 5){
                cout << "Điểm đối xứng qua trục Ox là: "; 
                print(reflection_x_axis(first));//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
            }
            //Kiểm tra thao tác và xử lý
            else if(choose == 5){
                cout << "Điểm đối xứng qua trục Ox là: "; 
                print(reflection_x_axis(first));//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
            }
            //Kiểm tra thao tác và xử lý
            else if(choose == 6){
                cout << "Điểm đối xứng qua trục Oy là: "; 
                print(reflection_y_axis(first));//In ra giá trị thao tác
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