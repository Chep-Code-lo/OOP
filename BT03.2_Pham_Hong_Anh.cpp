//Phạm Hồng Anh
#include <iostream>
using namespace std;
class ClntArray{
    private:
        int* array;
        int size;
        int capacity;
        //Phạm Hồng Anh
        void ensureCapacity(int need){
            if(need <= capacity) return;
            int newcap = (capacity == 0 ? 1 : capacity);
            while(newcap < need) newcap <<= 1;
            int* newArr = new int[newcap];
            for(int i=0; i<size; ++i) newArr[i] = array[i];
            delete[] array;
            array = newArr;
            capacity = newcap;
        }
    public:
    //Phạm Hồng Anh
    ClntArray(): array(nullptr), size(0), capacity(0) {}
    //Phạm Hồng Anh
    ClntArray(int* p, int n): array(nullptr), size(0), capacity(0){
        if(n > 0){
            capacity = n;
            array = new int[capacity];
            for(int i=0; i<n; ++i) array[i] = p[i];
            size = n;
        }
    }
    //Phạm Hồng Anh
    ClntArray(const ClntArray& o) : array(nullptr), size(0), capacity(0){
        if(o.size > 0){
            capacity = o.size;
            array = new int[capacity];
            for(int i=0; i<o.size; ++i) array[i] = o.array[i];
            size = o.size;
        }
    }
    //Phạm Hồng Anh
    ~ClntArray(){ delete[] array; }
    //Phạm Hồng Anh
    void input(){
        for(int i=0; i<size; ++i)
            cin >> array[i];
    }
    //Phạm Hồng Anh
    void print(){
        for(int i=0; i<size; ++i)
            cout << array[i] << " ";
        cout << "\n";
    }
    //Phạm Hồng Anh
    void addElement(int val){
        ensureCapacity(size + 1);
        array[size++] = val;
    }
    //Phạm Hồng Anh
    void addElement(int *p, int n){
        if(n <= 0) return;
        ensureCapacity(size + n);
        int cnt = 0;
        for(int i=size; i<size+n; ++i)
            array[i] = p[cnt++];
        size += n;
    }
    //Phạm Hồng Anh
    int getElement(int idx){
        return array[idx];
    }
    //Phạm Hồng Anh
    int getSize(){
        return size;
    }
    //Phạm Hồng Anh
    int getSum(){
        int sum = 0;
        for(int i=0; i<size; ++i)
            sum += array[i];
        return sum;
    }
    //Phạm Hồng Anh
    int getMax(){
        int max = -1e9;
        for(int i=0; i<size; ++i)
            if(array[i] > max)
                max = array[i];
        return max;
    }
    //Phạm Hồng Anh
    ClntArray getEven(){
        int cnt = 0;
        for(int i=0; i<size; ++i)
            if(array[i] % 2 == 0)
                cnt++;
        ClntArray temp;
        if(cnt > 0){
            temp.capacity = cnt;
            temp.array = new int [temp.capacity];
        }
        for(int i=0; i<size; ++i)
            if(array[i] % 2 == 0) temp.addElement(array[i]);
        return temp;
    }
    //Phạm Hồng Anh
    void erase(int idx){
        for(int i=idx; i<size-1; ++i)
            array[i] = array[i+1];
        size--;
    }
    //Phạm Hồng Anh
    void insert(int idx, int val){
        ensureCapacity(size + 1);
        for(int i=size; i>idx; --i) 
            array[i] = array[i-1];
        array[idx] = val;
        size++;
    }
    //Phạm Hồng Anh
    ClntArray operator+(const ClntArray &b) const{
        ClntArray res(*this);
        res.ensureCapacity(res.size + b.size);
        for(int i=0; i<b.size; ++i) res.array[res.size++] = b.array[i];
        return res;
    }
    //Phạm Hồng Anh
    ClntArray& operator=(const ClntArray &o){
        if(this == &o) return *this;
        int *na = nullptr;
        if(o.size > 0){
            na = new int[o.size];
            for (int i = 0; i < o.size; ++i) na[i] = o.array[i];
        }
        delete[] array;
        array = na; size = o.size; capacity = o.size;
        return *this;
    }
    //Phạm Hồng Anh
    ClntArray& operator++(){
        addElement(0);
        return *this;
    }
    //Phạm Hồng Anh
    ClntArray operator++(int){
        ClntArray tmp(*this);
        addElement(0);
        return tmp;
    }
    //Phạm Hồng Anh
    ClntArray& operator--(){
        if(size > 0) --size;
        return *this;
    }
    //Phạm Hồng Anh
    ClntArray operator--(int){
        ClntArray tmp(*this);
        if(size > 0) --size;
        return tmp;
    }
    //Phạm Hồng Anh
    bool operator==(const ClntArray &o) const{
        if(size != o.size) return false;
        for(int i=0; i<size; ++i)
            if(array[i] != o.array[i]) return false;
        return true;
    }
    //Phạm Hồng Anh
    bool operator<(const ClntArray &o) const{
        int n = min(size, o.size);
        for(int i=0; i<n; ++i){
            if(array[i] < o.array[i]) return true;
            if(array[i] > o.array[i]) return false;
        }
        return size < o.size;
    }
    //Phạm Hồng Anh 
    bool operator>(const ClntArray &o) const{
        return o < *this;
    }
    //Phạm Hồng Anh
    friend istream& operator>>(istream &is, ClntArray &arr){
        int n; is >> n;
        arr.ensureCapacity(n);
        arr.size = n;
        for(int i=0; i<n; ++i)  is >> arr.array[i];
        return is;
    }
    //Phạm Hồng Anh
    friend ostream& operator<<(ostream &os, const ClntArray &arr){
        for(int i=0; i<arr.size; ++i)
            os << arr.array[i] << " ";
        return os;
    }
};  
int main(){
    /*int a[] = {1, 2, 3, 4, 5, 6};
    ClntArray arr(a, 6);
    int b[] = {7, 8, 9, 10, 11, 12};
    ClntArray ar(b, 6);
    cout << "Mảng a "; arr.print();
    cout << "Mảng b "; ar.print();
    arr.addElement(8);
    cout << "Sau addElement(8): "; arr.print();
    arr.addElement(b, 6);
    cout << "Sau thêm b: "; arr.print();
    cout << "Giá trị lấy tại vị trí 4: " << arr.getElement(4) << "\n"; 
    cout << "Size=" << arr.getSize()
         << ", Sum=" << arr.getSum()
         << ", Max=" << arr.getMax() << "\n";
    ClntArray evenArr = arr.getEven();
    cout << "Mảng số chẵn: "; evenArr.print();
    arr.erase(4);
    cout << "Sau khi xóa phần tử tại vị trí 4: "; arr.print();
    arr.insert(2, 100);
    cout << "Sau insert(2,100): "; arr.print();*/

    int a[] = {1, 2, 3};
    int b[] = {1, 5};
    ClntArray arr(a, 3), ar(b, 2);

    cout << "arr = " << arr << "\n";
    cout << "ar  = " << ar << "\n";

    ClntArray c = arr + ar;
    cout << "arr + ar = " << c << "\n";

    cout << boolalpha;

    cout << "arr == ar? " << (arr == ar) << "\n";
    cout << "arr < ar? " << (arr < ar) << "\n";
    cout << "arr > ar? " << (arr > ar) << "\n";

    ClntArray d;
    d = arr;
    cout << "d = arr => d = " << d << "\n";

    cout << "++arr = " << (++arr) << " (arr = " << arr << ")\n";
    cout << "arr++ = " << (arr++) << " (arr = " << arr << ")\n";
    cout << "--arr = " << (--arr) << " (arr = " << arr << ")\n";
    cout << "arr-- = " << (arr--) << " (arr = " << arr << ")\n";

    cout << "Nhap mang (so phan tu + cac gia tri): ";
    ClntArray x;
    cin >> x;
    cout << "Mang vua nhap: " << x << "\n";
}