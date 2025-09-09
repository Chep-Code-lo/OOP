#include <iostream>
#include <limits>
#include <exception>
#include <stdexcept>
#include <cmath>
using namespace std;
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
struct triangle{
    double x1, y1, x2, y2, x3, y3;
};
//Tính diện tích có hướng 
static inline double cross2(const triangle &edge){
    return (edge.x2 -edge.x1)*(edge.y3 - edge.y1) - (edge.y2 - edge.y1)*(edge.x3 - edge.x1);
}
//Kiểm tra 3 điểm có thẳng hàng không
static inline bool collinear(const triangle &edge){
    const double eps = 1e-9;//Sai số epsilon
    return fabs(cross2(edge)) < eps;
}
//Tính bình phương khoảng cách giữa hai điểm
static inline double dist2(double x1, double y1, double x2, double y2){
    return (x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2);
}
//Kiểm tra gần bằng
static inline bool equal(double a, double b, double eps=1e-9){ return fabs(a-b) < eps; }
//Kiểm tra lớn hơn
static inline bool greater_than(double a, double b, double eps=1e-9){ return a > b + eps; }
//Kiểm tra giác đó có phải tam giác vuông không
void is_right_triangle(const triangle &edge){
    double AB2 = dist2(edge.x1, edge.y1, edge.x2, edge.y2);
    double BC2 = dist2(edge.x2, edge.y2, edge.x3, edge.y3);
    double CA2 = dist2(edge.x3, edge.y3, edge.x1, edge.y1);
    const double eps = 1e-9;//Sai số epsilon
    if(equal(AB2+BC2,CA2) || equal(AB2+CA2,BC2) || equal(BC2+CA2,AB2))
        cout << "Tam giác này là tam giác vuông";
    else cout << "Tam giác này không phải là tam giác vuông";
}
//Phân loại tam giác
string triangle_type(const triangle &edge){
    // Bình phương độ dài 3 cạnh
    double AB2 = dist2(edge.x1, edge.y1, edge.x2, edge.y2);
    double BC2 = dist2(edge.x2, edge.y2, edge.x3, edge.y3);
    double AC2 = dist2(edge.x1, edge.y1, edge.x3, edge.y3);
    // Sắp xếp để có x <= y <= z 
    double x = AB2, y = BC2, z = AC2;
    if (x > y) swap(x, y);
    if (y > z) swap(y, z);
    if (x > y) swap(x, y);  
    // Phân loại theo cạnh
    bool equilateral = equal(AB2, BC2) && equal(BC2, AC2);
    bool isosceles  = equilateral || equal(AB2, BC2) || equal(BC2, AC2) || equal(AB2, AC2);
    // Phân loại theo góc
    bool right  = equal(x + y, z);
    bool obtuse = greater_than(z, x + y);     // x + y < z
    bool acute  = greater_than(x + y, z);     // x + y > z
    if (equilateral)                 return "Tam giác đều";
    if (right && isosceles)          return "Tam giác vuông cân";
    if (right)                       return "Tam giác vuông";
    if (isosceles && acute)          return "Tam giác cân (nhọn)";
    if (isosceles && obtuse)         return "Tam giác cân (tù)";
    if (acute)                       return "Tam giác nhọn";
    if (obtuse)                      return "Tam giác tù";
    return "Tam giác thường";
}
//Tìm trọng tâm tam giác
void centroid(const triangle &edge){
    double g1 = (edge.x1 +edge.x2 + edge.x3) / 3.0;
    double g2 = (edge.y1 + edge.y2 + edge.y3) / 3.0;
    cout << "Trong tâm tam giác G: " << g1 << " " << g2;
}
//Nhập tọa độ điểm và kiểm tra điều kiện để tạo thành đúng tam giác không
void input(triangle &edge){
    while(true){
        cout << "Nhập vào tọa độ điểm A(x y): "; cin >> edge.x1 >> edge.y1;
        cout << "Nhập vào tọa độ điểm B(x y): "; cin >> edge.x2 >> edge.y2;
        cout << "Nhập vào tọa độ điểm C(x y): "; cin >> edge.x3 >> edge.y3;
        //Kiểm tra có nhập lỗi kiểu dữ liệu không
        if(!cin){
            cin.clear();//xóa trạng thái lỗi
            cin.ignore(std::numeric_limits<streamsize>::max(), '\n'); //Bỏ hết phần còn lại trong dòng nhập
            cout << "Nhập sai định dạng, Vui lòng nhập lại!!!";
            continue;
        }
        //Kiểm tra điều kiện nhập không phải là tam giác mà là đường thẳng
        if(collinear(edge)){
            cout << "Ba điểm thẳng hàng -> Không tạo thành tam giác, Vui lòng nhập lại!!!";
            continue;
        }
        break;//Hợp lệ thì thoát khỏi vòng lặp
    }
}
//Xuất tam giác
void print(const triangle &edge){
    cout << "Tam giác có tọa độ: "
         << "A(" << edge.x1 << ", " << edge.y1 << ") "
         << "B(" << edge.x2 << ", " << edge.y2 << ") "
         << "C(" << edge.x3 << ", " << edge.y3 << ")\n";
}
int main(){
    triangle edge;
    bool has_input = false;
    while(true){
        clear_screen();//Xóa màn hình trước khi in ra menu mới;
        //Xây dựng màn hình menu
    	cout << "\n==== MENU ====\n";
    	cout << "1) Nhập tam giác \n";
    	cout << "2) Xuất tam giác \n";
    	cout << "3) Kiểm tra tam giác vuông \n";
    	cout << "4) Cho biết loại tam giác \n";
    	cout << "5) Tìm trọng tâm tam giác \n";
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
                input(edge);//Nhập dữ liệu
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
    			cout << "Tam giác hiện tại hiện tại: "; print(edge);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
            else if(choose == 3){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			is_right_triangle(edge);//In ra giá trị thao tácc
                pause_enter();//Dừng tạm thời
    		}
            else if(choose == 4){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			triangle_type(edge);//In ra giá trị thao tácc
                pause_enter();//Dừng tạm thời
    		}
            else if(choose == 5){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			centroid(edge);
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