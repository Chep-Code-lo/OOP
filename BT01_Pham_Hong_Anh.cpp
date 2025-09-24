//Phạm Hồng Anh
#include <iostream>
#include <algorithm>
using namespace std;
class SPhanSo{
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
        SPhanSo Cong(SPhanSo y){
            SPhanSo result;
            result.set(tu * y.mau + y.tu * mau, mau * y.mau);  
            return result;
        }
        //Phạm Hồng Anh
        SPhanSo nhan(SPhanSo y){
            SPhanSo result;
            result.set(tu * y.tu, mau * y.mau);  
            return result;
        }
        //Phạm Hồng Anh
        bool soSanhBang(SPhanSo y){
            return (tu*y.mau == y.tu*mau);
        }
        //Phạm Hồng Anh
        bool soSanhBe(SPhanSo y){
            return (tu*y.mau < y.tu*mau);
        }
        //Phạm Hồng Anh
        SPhanSo& gan(SPhanSo y){
            tu = y.tu;
            mau = y.mau;
            return *this;
        }
        //Phạm Hồng Anh
        SPhanSo& gan(int k){
            tu = k;
            mau = 1;
            return *this;
        }
};
int main(){
    SPhanSo x, y;
    cout << "Nhập phân số X: "; x.nhap();
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
    cout << "Phân số X khi gán K là: "; x.gan(k).xuat();
}