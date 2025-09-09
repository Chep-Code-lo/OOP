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
    //Nạp chồng toán tử so sánh cho kiểu dữ liệu fraction
    bool operator<(const fraction &other) const{
        return numerator*other.denominator < denominator*other.numerator;
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
    void output() const{
        cout << numerator << "/" << denominator << "\n";
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
//Nhập một phân số từ bàn phím và đảm bảo dữ liệu đó là hợp lệ
void read_fraction(fraction &p, int name){
	while(true){
        cout << "Nhập phân số thứ " << name + 1 << " (tử mẫu): ";
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
//Nhập mảng phân số
void input(vector<fraction> &ds){
    cout << "Nhập số lượng phân số: ";
    int n; cin >> n;
    for(int i = 0; i < n; ++i) {
        fraction ps;
        read_fraction(ps, i);
        ps.simplify();
        ds.push_back(ps);
    }
}
//In ra màn hình mảng phân số tử số và mẫu số được cách nhau bởi dấu cách và xuống dòng khi in ra mẫu số
void print(const vector<fraction> &ds){
    cout << " ";
    for(std::size_t i = 0; i < ds.size(); ++i){
        cout << ds[i].numerator << "/" << ds[i].denominator << "\n";
        if (i + 1 < ds.size()) cout << " ";
    }
}
//Liệt kê các phân số âm
static vector<fraction> list_or_negative_fractions(const vector<fraction> &ds){
    vector<fraction>add_minus;//Lưu phân số âm
    for(const auto &x : ds){
        if(1LL*x.numerator*x.denominator < 0)
            add_minus.push_back({x.numerator, x.denominator});
    }
    return add_minus;
}
//Tính tổng mảng phân số
fraction sum(const vector<fraction> &ds){
    fraction sum = {0, 1};
    for(const auto &ps : ds)
        sum = sum + ps;
    return sum;
}
//Tìm phân số lớn nhất
fraction find_max(const vector<fraction> &ds){
    return *max_element(ds.begin(), ds.end(), 
        [](const fraction &a, const fraction &b)
            {return !(a > b);} 
    );
}
fraction find_max_minus(const vector<fraction> &ds){
    auto temp = list_or_negative_fractions(ds);
    return *max_element(temp.begin(), temp.end(),
        [](const fraction &a, const fraction &b)
            {return a < b; }
    );
}
int main(){
    vector<fraction> first;
    bool has_input = false;
    while(true){
        clear_screen();// Xóa màn hình chờ trước khi in ra màn hình chính
        //Xây dựng màn hình menu
    	cout << "\n==== MENU ====\n";
    	cout << "1) Nhập mảng phân số \n";
    	cout << "2) Xuất mảng phân số \n";
    	cout << "3) Liệt kê các phân số âm có trong mảng \n";
    	cout << "4) Tính tổng mảng phân số \n";
    	cout << "5) Đếm số lượng phân số âm có trong mảng \n";
    	cout << "6) Tìm phân số lớn nhất có trong mảng \n";
    	cout << "7) Tìm phân số âm lớn nhất có trong mảng \n";
    	cout << "8) Kiểm tra xem trong mảng có tồn tại số âm nào không \n";
        cout << "9) Kiểm tra xem có phải tất cả các phân số trong mảng đều âm hay không \n";
    	cout << "0) thoát chương trình \n";
    	cout << "Chọn: ";
    	int choose = read_choice(0, 9);//Biến lựa chọn 
        //Xử dụng cấu trúc try để xử lý các thao tác để dễ xử lý lỗi
        try{
            //Kiểm tra và xử lý thao tác
            	if(choose == 0){
    			cout << "Tạm biệt !\n";
    			break;
    		}
            //Kiểm tra thao tác và xử lý
    		else if(choose == 1){
    			input(first);//Thao tác nhập mảng phân số
    			has_input = true;//Đánh dấu đã được thêm input
                pause_enter();//Dừng tạm thời
    		}
            //Kiểm tra thao tác và xử lý
    		else if(choose == 2){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << "Mảng phân số hiện tại: \n"; print(first);//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
              //Kiểm tra thao tác và xử lý
    		else if(choose == 3){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << "Mảng phân số âm hiện tại: \n";   print(list_or_negative_fractions(first));//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
              //Kiểm tra thao tác và xử lý
    		else if(choose == 4){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << "Tổng mảng phân số : "; sum(first).output();//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
              //Kiểm tra thao tác và xử lý
    		else if(choose == 5){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << "Số lượng phân số âm hiện tại: ";
                auto temp = list_or_negative_fractions(first);
                cout << temp.size() << "\n";
                pause_enter();//Dừng tạm thời
    		}
              //Kiểm tra thao tác và xử lý
    		else if(choose == 6){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << "Phân số lớn nhất có trong mảng: "; find_max(first).output();//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
              //Kiểm tra thao tác và xử lý
    		else if(choose == 7){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			cout << "Phân số âm lớn nhất có trong mảng: "; find_max_minus(first).output();//In ra giá trị thao tác
                pause_enter();//Dừng tạm thời
    		}
              //Kiểm tra thao tác và xử lý
    		else if(choose == 8){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			auto temp = list_or_negative_fractions(first);
                if(temp.size() == 0) cout << "Mảng không có tồn tại phân số âm nào\n";
                else cout << "Mảng có tồn tại phân số âm\n";
                pause_enter();//Dừng tạm thời
    		}
              //Kiểm tra thao tác và xử lý
    		else if(choose == 9){
                //Kiểm tra nếu input chưa được nhập thì yêu cầu chọn thao tác nhập lại
    			if(!has_input){
    				cout << "Chưa có dữ liệu được nhập! Hãy chọn 1 để nhập!\n ";
                    pause_enter();//Dừng tạm thời
    				continue;//Quay lại vòng lặp để nhập lại
    			}
    			auto temp = list_or_negative_fractions(first);
                if(temp.size() == first.size()) cout << "Tất cả các phân số có trong mảng đều là phân số âm\n";
                else cout << "Tất cả các phân số có trong mảng đều không phải là phân số âm\n";
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