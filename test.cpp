#include <bits/stdc++.h>
using namespace std;

struct student{
    string student_code, name, sex; // sex lưu dạng gốc người dùng nhập
    double point;
    void output() const {
        cout << student_code << " " << name << " " << sex << " " << point << "\n";
    }
};

struct student_rank{
    string student_code, name, sex;
    double point;
    string rank;
    void output() const {
        cout << student_code << " " << name << " " << sex << " " << point << " " << rank << "\n";
    }
};

// --------- Tiện ích nhập số nguyên trong [min,max] ----------
int read_choice(int min_v, int max_v){
    while (true){
        int choose;
        if(!(cin >> choose)){
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Vui lòng nhập số!\nChọn: ";
            continue;
        }
        if(choose < min_v || choose > max_v){
            cout << "Lựa chọn không hợp lệ! Nhập trong [" << min_v << "," << max_v << "]\nChọn: ";
            continue;
        }
        cin.ignore(numeric_limits<streamsize>::max(), '\n'); // bỏ enter còn lại
        return choose;
    }
}

void clear_screen(){
#ifdef _WIN32
    system("cls");
#else
    system("clear");
#endif
}

void pause_enter(){
    cout << "Nhấn Enter để tiếp tục..."; 
    cin.ignore(numeric_limits<streamsize>::max(), '\n');
}

// --------- Chuẩn hoá không dấu cho tiếng Việt ngắn gọn ----------
static inline string to_lower_ascii(string s){
    for (auto &c : s) c = (char)tolower((unsigned char)c);
    return s;
}
static inline string remove_vn_diacritics(string s){
    const pair<const char*, char> map[] = {
        {"á",'a'},{"à",'a'},{"ả",'a'},{"ã",'a'},{"ạ",'a'},
        {"ă",'a'},{"ắ",'a'},{"ằ",'a'},{"ẳ",'a'},{"ẵ",'a'},{"ặ",'a'},
        {"â",'a'},{"ấ",'a'},{"ầ",'a'},{"ẩ",'a'},{"ẫ",'a'},{"ậ",'a'},
        {"é",'e'},{"è",'e'},{"ẻ",'e'},{"ẽ",'e'},{"ẹ",'e'},
        {"ê",'e'},{"ế",'e'},{"ề",'e'},{"ể",'e'},{"ễ",'e'},{"ệ",'e'},
        {"í",'i'},{"ì",'i'},{"ỉ",'i'},{"ĩ",'i'},{"ị",'i'},
        {"ó",'o'},{"ò",'o'},{"ỏ",'o'},{"õ",'o'},{"ọ",'o'},
        {"ô",'o'},{"ố",'o'},{"ồ",'o'},{"ổ",'o'},{"ỗ",'o'},{"ộ",'o'},
        {"ơ",'o'},{"ớ",'o'},{"ờ",'o'},{"ở",'o'},{"ỡ",'o'},{"ợ",'o'},
        {"ú",'u'},{"ù",'u'},{"ủ",'u'},{"ũ",'u'},{"ụ",'u'},
        {"ư",'u'},{"ứ",'u'},{"ừ",'u'},{"ử",'u'},{"ữ",'u'},{"ự",'u'},
        {"ý",'y'},{"ỳ",'y'},{"ỷ",'y'},{"ỹ",'y'},{"ỵ",'y'},
        {"đ",'d'}
    };
    for (auto [from, to] : map){
        size_t pos = 0, k = strlen(from);
        while ((pos = s.find(from, pos)) != string::npos) s.replace(pos, k, string(1, to));
    }
    return s;
}
static inline string normalize_sex_key(string s){
    // về chữ thường + bỏ khoảng trắng đầu/cuối + bỏ dấu
    while(!s.empty() && isspace((unsigned char)s.front())) s.erase(s.begin());
    while(!s.empty() && isspace((unsigned char)s.back()))  s.pop_back();
    s = to_lower_ascii(s);
    s = remove_vn_diacritics(s); // "nữ" -> "nu", "khác" -> "khac"
    return s;
}

// ---------- Nhập 1 sinh viên ----------
void in(student &s, int id){
    cout << "Thông tin sinh viên thứ " << id << "\n";
    cout << "Nhập MSSV: ";
    cin >> s.student_code;
    cin.ignore(numeric_limits<streamsize>::max(), '\n');

    cout << "Nhập họ tên: ";
    getline(cin, s.name);

    while(true){
        cout << "Nhập giới tính (nam/nu/khac | có thể gõ có dấu): ";
        string raw; getline(cin, raw);
        string key = normalize_sex_key(raw);
        if (key == "nam")  { s.sex = "Nam";  break; }
        if (key == "nu")   { s.sex = "Nữ";   break; }
        if (key == "khac") { s.sex = "Khác"; break; }
        cout << "Giới tính không hợp lệ! Vui lòng nhập lại.\n";
    }

    while(true){
        cout << "Nhập điểm (0 - 10): ";
        if(!(cin >> s.point)){
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Vui lòng nhập số!\n";
            continue;
        }
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        if(s.point < 0 || s.point > 10.0){
            cout << "Điểm phải nằm trong khoảng [0,10]. Nhập lại.\n";
            continue;
        }
        break;
    }
}

// ---------- Nhập danh sách ----------
void input(vector<student> &ds){
    cout << "Nhập vào số lượng sinh viên: ";
    int n;
    while(!(cin >> n) || n <= 0){
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        cout << "Vui lòng nhập số nguyên dương!\nNhập lại: ";
    }
    cin.ignore(numeric_limits<streamsize>::max(), '\n');
    ds.clear();
    ds.reserve(n);
    for(int i = 1; i <= n; ++i){
        student tmp;
        in(tmp, i);
        ds.push_back(tmp);
    }
}

// ---------- Chuẩn hoá tên & giới tính để in đẹp ----------
string normalize_name(const student &s){
    string name = s.name;
    // trim
    while(!name.empty() && isspace((unsigned char)name.front())) name.erase(name.begin());
    while(!name.empty() && isspace((unsigned char)name.back()))  name.pop_back();
    // lower
    for(char &c : name) c = (char)tolower((unsigned char)c);
    // rút gọn khoảng trắng
    string mid; bool sp = false;
    for(char c : name){
        if(isspace((unsigned char)c)){ if(!sp){ mid += ' '; sp = true; } }
        else { mid += c; sp = false; }
    }
    // viết hoa chữ cái đầu
    string res; bool cap = true;
    for(char c : mid){
        if(c == ' '){ res += c; cap = true; }
        else{ res += cap ? (char)toupper((unsigned char)c) : c; cap = false; }
    }
    return res;
}

// ---------- In DS sinh viên (đẹp) ----------
void print(const vector<student> &s){
    for (size_t i = 0; i < s.size(); ++i){
        student t = s[i];
        t.name = normalize_name(t);
        // t.sex đã là "Nam/Nữ/Khác"
        t.output();
    }
}

// ---------- Qua môn ----------
static int count_pass = 0;
static vector<student> add_pass;

static bool is_pass(const student &s){ return s.point >= 5.0; }

void build_pass(const vector<student> &s){
    count_pass = 0;
    add_pass.clear();
    for (const auto &st : s){
        if (is_pass(st)){
            ++count_pass;
            student t = st; t.name = normalize_name(t);
            add_pass.push_back(t);
        }
    }
}
void print_pass(const vector<student> &s){
    build_pass(s);
    for (const auto &st : add_pass) st.output();
}

// ---------- Xếp loại ----------
string rank_student(const student &s){
    if (s.point < 4)        return "Sinh viên này xếp loại kém!!!!!";
    else if (s.point < 5)   return "Sinh viên này xếp loại yếu!!!!";
    else if (s.point < 7)   return "Sinh viên này xếp loại trung bình!!!";
    else if (s.point < 8)   return "Sinh viên này xếp loại khá!!";
    else if (s.point < 9)   return "Sinh viên này xếp loại giỏi!";
    else if (s.point <= 10) return "Sinh viên này xếp loại xuất sắc";
    return "Điểm không hợp lệ!!!"; // để compiler không cảnh báo
}

static vector<student_rank> add_rank;

void build_rank(const vector<student> &s){
    add_rank.clear();
    for (const auto &st : s){
        student_rank r{
            st.student_code,
            normalize_name(st),
            st.sex,
            st.point,
            rank_student(st)
        };
        add_rank.push_back(r);
    }
}
void print_rank(const vector<student> &s){
    build_rank(s);
    for (const auto &r : add_rank) r.output();
}

// ---------- Trung bình ----------
void average_core(const vector<student> &s){
    if (s.empty()){ cout << "0\n"; return; }
    double sum = 0.0;
    for (const auto &st : s) sum += st.point;
    cout << fixed << setprecision(2) << (sum / (double)s.size()) << "\n";
}

// ---------- Điểm lớn nhất ----------
void student_point_max(const vector<student> &s){
    if (s.empty()){ cout << "(không có)"; return; }
    size_t idx = 0;
    double best = s[0].point;
    for (size_t i = 1; i < s.size(); ++i){
        if (s[i].point > best){ best = s[i].point; idx = i; }
    }
    student res = s[idx];
    cout << normalize_name(res) << "\n";
}

// ---------- Có nữ loại giỏi? ----------
void student_girl_point_good(const vector<student> &s){
    build_rank(s);
    for (const auto &r : add_rank){
        if (r.sex == "Nữ" && r.rank == "Sinh viên này xếp loại giỏi!"){
            cout << "Có sinh viên nữ đạt loại giỏi\n";
            return;
        }
    }
    cout << "Không có sinh viên nữ đạt loại giỏi\n";
}

int main(){
    vector<student> s;
    bool has_input = false;

    while(true){
        clear_screen();
        cout << "\n==== MENU ====\n";
        cout << "1) Nhập danh sách sinh viên\n";
        cout << "2) Xuất danh sách sinh viên (đẹp)\n";
        cout << "3) In những sinh viên qua môn\n";
        cout << "4) In bảng xếp loại sinh viên\n";
        cout << "5) Cho biết tỉ lệ sinh viên qua môn\n";
        cout << "6) Tính điểm trung bình các sinh viên\n";
        cout << "7) Cho biết tên sinh viên có điểm cao nhất\n";
        cout << "8) Cho biết có nữ đạt loại giỏi không\n";
        cout << "0) Thoát chương trình\n";
        cout << "Chọn: ";

        int choose = read_choice(0, 8);

        try{
            if (choose == 0){
                cout << "Tạm biệt!\n";
                break;
            } else if (choose == 1){
                input(s);
                has_input = true;
                pause_enter();
            } else {
                if (!has_input){
                    cout << "Chưa có dữ liệu! Hãy chọn 1 để nhập.\n";
                    pause_enter();
                    continue;
                }
                if (choose == 2){
                    cout << "Thông tin sinh viên hiện tại:\n";
                    print(s);
                    pause_enter();
                } else if (choose == 3){
                    cout << "Sinh viên qua môn:\n";
                    print_pass(s);
                    pause_enter();
                } else if (choose == 4){
                    cout << "Bảng xếp loại sinh viên:\n";
                    print_rank(s);
                    pause_enter();
                } else if (choose == 5){
                    build_pass(s);
                    double rate = s.empty()? 0.0 : (double)count_pass / (double)s.size() * 100.0;
                    cout << "Tỉ lệ sinh viên qua môn: " << fixed << setprecision(2) << rate << "%\n";
                    pause_enter();
                } else if (choose == 6){
                    cout << "Điểm trung bình của các sinh viên: ";
                    average_core(s);
                    pause_enter();
                } else if (choose == 7){
                    cout << "Tên sinh viên có điểm cao nhất là: ";
                    student_point_max(s);
                    pause_enter();
                } else if (choose == 8){
                    student_girl_point_good(s);
                    pause_enter();
                }
            }
        } catch (const exception &e){
            cout << "Lỗi: " << e.what() << "\n";
            pause_enter();
        }
    }
    return 0;
}
