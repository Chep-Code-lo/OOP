#include<iostream>
using namespace std;
struct Phanso{
    int tu, mau;
};
void nhapps(Phanso *x){
    cout << "Nhập phân số";
    cin >> x -> tu >> x -> mau;
}
void xuatps(Phanso *x){
    cout << "Phân số là: ";
    cout << x -> tu << " " << x -> mau;
}
int main(){
    Phanso *a = new Phanso;
}