#include <iostream>
#include <algorithm>
#include <vector>
#include <string>
#include <limits>
#include <exception>
#include <stdexcept>
using namespace std;
struct fraction{
    int numerator, denominator;//Biến thành viên của struct
    //Rút gọn phân số
    void simplify(){
        int gcd = __gcd(numerator, denominator);
        numerator /= gcd;
        denominator /= gcd;
        if(denominator < 0){
            numerator = -numerator;
            denominator = -denominator;
        }
    }
    //Nạp chồng toán tử so sánh cho kiểu dữ liệu fraction
    bool operator>(const fraction &other) const{
        return numerator * other.denominator > denominator * other.numerator;
    }
    //Nạp chồng toán tử cộng cho kiểu dữ liệu fraction
    fraction operator+(const fraction &other) const{
        fraction result = {numerator * other.denominator + other.numerator * denominator, denominator * other.denominator};
        result.simplify();
        return result;
    }
    //Nạp chồng toán tử nhân cho kiểu dữ liệu fraction
    fraction operator*(const fraction &other) const{
        fraction result = {numerator * other.numerator, denominator * other.denominator};
        result.simplify();
        return result;
    }
    //Nạp chồng toán tử trừ cho kiểu dữ liệu fraction
    fraction operator-(const fraction &other) const{
        fraction result = {numerator * other.denominator - other.numerator * denominator, denominator * other.denominator};
        result.simplify();
        return result;
    }
    //Nạp chồng toán tử chia cho kiểu dữ liệu fraction
    fraction operator/(const fraction &other) const{
        if(other.numerator == 0) throw runtime_error("Chia cho 0");
    	fraction result{numerator * other.denominator, denominator * other.numerator};
    	result.simplify();
    	return result; 
    }
};
//In ra màn hình giá trị tử số và mẫu số được cách nhau bởi dấu cách và xuống dòng khi in ra mẫu số
void print (const fraction &p){
	cout << p.numerator << " " << p.denominator << "\n";
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
//Nhập một phân số từ bàn phím và đảm bảo dữ liệu đó là hợp lệ
void read_fraction(fraction &p, const string &name){
	while(true){
		cout << "Nhập " << name << "(tử mẫu): ";
        //Kiểm tra nếu nhập không phải số nguyên
		if(!(cin >> p.numerator >> p.denominator)){
			cin.clear();//Xóa trạng thái lỗi của cin
			cin.ignore(std :: numeric_limits<streamsize>::max(), '\n');//Bỏ hết phần còn lại trong dòng nhập
			cout << "Vui lòng nhập lại số! \n";
			continue;//Quay lại vòng lặp để nhập lại
		}
        //Kiểm tra giá trị mẫu
		if(p.denominator == 0){
			cout << "Mẫu số phải khác 0!, Vui lòng nhập lại!!! \n";
			continue;//Quay lại vòng lặp để nhập lại
		}
		p.simplify();//Rút gọn phân số
		break;//Thỏa mãn các điều kiện thì thoát khỏi vòng lặp
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
int main(){
    fraction first;
    bool has_input = false;//Đặt cờ cho input
    while(true){
        clear_screen();//Xóa màn hình trước khi in menu mới
        //Xây dựng màn hình menu
    	cout << "\n==== MENU ====\n";
    	cout << "1) Nhập phân số \n";
    	cout << "2) Xuất phân số \n";
    	cout << "3) Rút gọn phân số \n";
    	cout << "4) Tổng phân số \n";
    	cout << "5) Hiệu phân số \n";
    	cout << "6) Tích phân số \n";
    	cout << "7) Thương phân số \n";
    	cout << "8) So sánh hai phân số \n";
    	cout << "0) thoát chương trình \n";
    	cout << "Chọn: ";
    	int choose = read_choice(0, 8);//Biến lựa chọn 
        //Xử dụng cấu trúc try để xử lý các thao tác để dễ xử lý lỗi
    	try {
            //Kiểm tra thao tác và xử lý
    		if(choose == 0){
    			cout << "Tạm biệt !\n";
    			break;
    		}
            //Kiểm tra thao tác và xử lý
    		else if(choose == 1){
    			read_fraction(first, "phân số");//Thao tác nhập phân số
    			has_input = true;//Đánh dấu đã được thêm input
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra thao tác và xử lý
    		else if(choose == 2){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có phân số được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << "Phân số hiện tại: "; print(first);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra thao tác và xử lý
    		else if(choose == 3){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có phân số được nhập! Hãy chọn 1 để nhập!\n";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			first.simplify();//Gọi hàm để rút gọn phân số
    			cout << "Phân số đã được rút gọn là: "; print(first);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra thao tác và xử lý
    		else {
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có phân số được nhập! Hãy chọn 1 để nhập!\n";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			fraction second;//Khai báo kiểu dữ liệu phân số thứ 2
    			cout << "Nhập thêm phân số thứ 2 để tiếp tục! \n";
                //Xử lý thao tác nhập thêm phân số thứ 2
                while(true){
                    read_fraction(second, "phân số thứ 2");//Nhập phân số
                    //Kiểm tra điều kiện tử số bằng 0 thì yêu cầu nhập lại
                    if(second.numerator == 0){
                        cout << "Tử bằng 0 nên không thể chia. Vui lòng nhập lại\n";
                        continue;//Quay lại vòng lặp để nhập lại
                    }
                    break;//Thỏa mãn yêu cầu thì thoát khỏi vòng lặp
                }
                //Kiểm tra lựa chọn và xử lý
    			if(choose == 4){
    				fraction result = first + second;
    				cout << "Tổng của hai phân số là: "; print(result);
    			}
                //Kiểm tra lựa chọn và xử lý
    			else if(choose == 5){
    				fraction result = first - second;
    				cout << "Hiệu của hai phân số là: "; print(result);
    			}
                //Kiểm tra lựa chọn và xử lý
    			else if(choose == 6){
    				fraction result = first * second;
    				cout << "Tích của hai phân số là: "; print(result);
    			}
                //Kiểm tra lựa chọn và xử lý
    			else if(choose == 7){
    				fraction result = first / second;
    				cout << "Thương của hai phân số là: "; print(result);
    			}
                //Kiểm tra lựa chọn và xử lý
    			else if(choose == 8){
    				if(first > second ) cout << "Phân số thứ nhất lớn hơn \n";
    				else if(second > first) cout << "Phân số thứ hai lớn hơn \n";
    				else cout << "Hai phân số bằng nhau \n";
    			}
                pause_enter();
    		}
    	}
        //Thông báo các lỗi được lấy ra từ cấu trúc try
        catch(const exception &e){
                cout << "Lỗi: " << e.what() << endl;//In ra lỗi cụ thể
                pause_enter();//Tạm dừng để nhìn lỗi
    	} 
    }
}
