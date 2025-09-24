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
            for (int i = 0; i < n; ++i) array[i] = p[i];
            size = n;
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
};  
int main(){
    int a[] = {1, 2, 3, 4, 5, 6};
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
    cout << "Sau insert(2,100): "; arr.print();
}