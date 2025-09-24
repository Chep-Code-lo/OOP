    //Phạm Hồng Anh
    #include <iostream>
    #include <algorithm>
    #include <stdexcept>
    using namespace std;
    class CPhanSo {
        private:
            int tu, mau;  
        public:
            //Phạm Hồng Anh
            void nhap(){
                cin >> tu >> mau;
            }
            //Phạm Hồng Anh
            void xuat(){
                cout << tu << "/"<< mau << "\n";
            }
            //Phạm Hồng Anh
            void rutGon(){
                if(mau == 0) return;
                int gcd = __gcd(tu, mau);
                tu /= gcd;
                mau /= gcd;
            } 
            //Phạm Hồng Anh
            void chuanHoa(){
                if(mau < 0){
                    tu = -tu;
                    mau = -mau;
                }
                if(tu == 0)
                    mau = 1;
            }
            //Phạm Hồng Anh
            void set(int tu, int mau){
                this->tu = tu;
                this->mau = mau;
                rutGon();
                chuanHoa();
            }
            //Phạm Hồng Anh
            CPhanSo  Cong(CPhanSo  y){
                CPhanSo  result;
                result.set(tu * y.mau + y.tu * mau, mau * y.mau);  
                return result;
            }
            //Phạm Hồng Anh
            CPhanSo  nhan(CPhanSo  y){
                CPhanSo  result;
                result.set(tu * y.tu, mau * y.mau);  
                return result;
            }
            //Phạm Hồng Anh
            bool soSanhBang(CPhanSo  y){
                return (tu*y.mau == y.tu*mau);
            }
            //Phạm Hồng Anh
            bool soSanhBe(CPhanSo  y){
                return (tu*y.mau < y.tu*mau);
            }
            //Phạm Hồng Anh
            CPhanSo & gan(CPhanSo  y){
                tu = y.tu;
                mau = y.mau;
                return *this;
            }
            //Phạm Hồng Anh
            CPhanSo & gan(int k){
                tu = k;
                mau = 1;
                return *this;
            }
            //Phạm Hồng Anh
            CPhanSo  operator+(const CPhanSo  &other) const{
                CPhanSo result;
                result.set(tu * other.mau + other.tu * mau, mau * other.mau);
                return result;
            }
            //Phạm Hồng Anh
            CPhanSo  operator-(const CPhanSo  &other) const{
                CPhanSo  result;
                result.set(tu * other.mau - other.tu * mau, mau * other.mau);
                return result;
            }
            //Phạm Hồng Anh
            CPhanSo  operator*(const CPhanSo  &other) const{
                CPhanSo  result;
                result.set(tu * other.tu, mau * other.mau);
                return result;
            }
            //Phạm Hồng Anh
            CPhanSo  operator/(const CPhanSo  &other) const{
                if(other.tu == 0) throw runtime_error("Chia cho 0");
                CPhanSo  result;
                result.set(tu * other.mau, mau * other.tu);
                return result; 
            }
            //Phạm Hồng Anh
            CPhanSo& operator++(){
                tu += mau;
                rutGon();
                return *this;
            }
            //Phạm Hồng Anh
            CPhanSo operator++(int){
                CPhanSo tmp = *this;
                ++(*this);
                return tmp;
            }
            //Phạm Hồng Anh
            CPhanSo operator--(){
                tu -= mau;
                rutGon();
                return *this;
            }
            //Phạm Hồng Anh
            CPhanSo operator--(int){
                CPhanSo tmp = *this;
                --(*this);
                return tmp;
            }
            //Phạm Hồng Anh
            bool operator==(const CPhanSo &other) const{return tu == other.tu && mau == other.mau;}
            //Phạm Hồng Anh
            bool operator<(const CPhanSo &other) const{
                return tu * other.mau < mau * other.tu;
            }
            //Phạm Hồng Anh
            bool operator>(const CPhanSo  &other) const{return other < *this;}
            //Phạm Hồng Anh
            bool operator<=(const CPhanSo &other) const{return !(other < *this);}
            //Phạm Hồng Anh
            bool operator>=(const CPhanSo &other) const {return !(*this < other);}
            //Phạm Hồng Anh
            bool operator!=(const CPhanSo &other) const {return !(*this == other);}
            //Phạm Hồng Anh
            CPhanSo& operator+=(const CPhanSo &other) {return *this = *this + other;}
            //Phạm Hồng Anh
            CPhanSo& operator-=(const CPhanSo &other) {return *this = *this - other;}
            //Phạm Hồng Anh
            CPhanSo& operator*=(const CPhanSo &other) {return *this = *this * other;}
            //Phạm Hồng Anh
            CPhanSo& operator/=(const CPhanSo &other) {return *this = *this / other;}
            //.* là toán tử pointer dùng để gọi thành viên của một object thông qua con trỏ nên nó không phải toán tử thông thường do đó không thể nạp chồng để làm việc khác được
            //Phạm Hồng Anh
            friend ostream& operator<<(ostream &os, const CPhanSo &PS){
                return os << PS.tu << "/" << PS.mau;
            }
            //Phạm Hồng Anh
            friend istream& operator>>(istream &is, CPhanSo &PS){
                int t, m;
                if(is >> t >> m){
                    PS.set(t, m);
                }
                return is;
            }

    };
    int main(){
        CPhanSo  x, y;
        /*cout << "Nhập phân số X: "; x.nhap();
        cout << "Nhập phân số Y: "; y.nhap();
        cout << "Phân số X có giá trị: "; x.xuat();
        x.rutGon();
        cout << "Phân số X sau khi rút gọn: "; x.xuat();
        x.chuanHoa();
        cout << "Phân số X sau khi chuẩn hóa: "; x.xuat();
        cout << "Phân số X cộng phân số Y là: "; x.Cong(y).xuat();
        cout << "Phân số X nhân phân số Y là: "; x.nhan(y).xuat();
        cout << "Phân số X bằng phân số Y là: " << (x.soSanhBang(y) ? "true" : "false") << "\n";
        cout << "Phân số X bé hơn phân số Y là: " <<(x.soSanhBe(y) ? "true" : "false") << "\n";
        cout << "Phân số X gán bằng phân số Y là: "; x.gan(y).xuat();
        cout << "Nhập giá trị K: ";
        int k; cin >> k;
        cout << "Phân số X khi gán K là: "; x.gan(k).xuat();*/

        cout << "Nhap X (a b): "; cin >> x;
        cout << "Nhap Y (a b): "; cin >> y;
        cout << "X = " << x << "\n";
        cout << "Y = " << y << "\n";

        cout << "X + Y = " << (x + y) << '\n';
        cout << "X - Y = " << (x - y) << '\n';
        cout << "X * Y = " << (x * y) << '\n';
        cout << "X / Y = " << (x / y) << '\n';

        cout << "X +=  Y? " << (x +=  y) << '\n';
        cout << "X -=  Y? " << (x -=  y) << '\n';
        cout << "X *=  Y? " << (x *=  y) << '\n';
        cout << "X /=  Y? " << (x /=  y) << '\n';

        cout << boolalpha;

        cout << "X == Y? " << (x == y) << '\n';
        cout << "X <  Y? " << (x <  y) << '\n';
        cout << "X >  Y? " << (x >  y) << '\n';
        cout << "X >=  Y? " << (x >=  y) << '\n';
        cout << "X <=  Y? " << (x <=  y) << '\n';
        
        cout << "++X = " << (++x) << '\n';
        cout << "X++ = " << (x++) << " (sau X = " << x << ")\n";
        cout << "--X = " << (--x) << '\n';
        cout << "X-- = " << (x--) << " (sau X = " << x << ")\n";
    }