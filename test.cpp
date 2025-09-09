#include <bits/stdc++.h>
using namespace std;

// ======= Tiện ích =======
static inline string now_str() {
    time_t t = time(nullptr);
    tm *lt = localtime(&t);
    char buf[64];
    strftime(buf, sizeof(buf), "%Y-%m-%d %H:%M:%S", lt);
    return string(buf);
}

struct Logger {
    static void info(const string& msg)  { write("INFO", msg);  }
    static void warn(const string& msg)  { write("WARN", msg);  }
    static void error(const string& msg) { write("ERROR", msg); }
private:
    static void write(const string& level, const string& msg) {
        ofstream f("security.log", ios::app);
        f << "[" << now_str() << "][" << level << "] " << msg << "\n";
    }
};

// ======= Băm mật khẩu (mô phỏng học thuật) =======
struct PasswordHasher {
    // FNV-1a 64-bit cho demo; KHÔNG dùng cho sản phẩm thật
    static uint64_t fnv1a64(const string& s) {
        const uint64_t FNV_OFFSET = 1469598103934665603ull;
        const uint64_t FNV_PRIME  = 1099511628211ull;
        uint64_t h = FNV_OFFSET;
        for (unsigned char c : s) {
            h ^= c;
            h *= FNV_PRIME;
        }
        return h;
    }
    static string rand_hex(size_t n=16) {
        static random_device rd;
        static mt19937_64 gen(rd());
        static uniform_int_distribution<int> dist(0, 15);
        const char* HEX="0123456789abcdef";
        string s; s.reserve(n);
        for(size_t i=0;i<n;i++) s.push_back(HEX[dist(gen)]);
        return s;
    }
    static string hash_pw(const string& password, const string& salt) {
        uint64_t h = fnv1a64(password + ":" + salt);
        stringstream ss; ss << hex << h;
        return ss.str();
    }
};

// ======= Mô hình dữ liệu =======
enum class Role { ADMIN, USER };

static inline string role_to_str(Role r) {
    return (r==Role::ADMIN) ? "ADMIN" : "USER";
}
static inline Role str_to_role(const string& s) {
    return (s=="ADMIN") ? Role::ADMIN : Role::USER;
}

struct UserRecord {
    string username;
    Role role = Role::USER;
    string salt;
    string hash;
    bool locked = false;
    int failed_attempts = 0;
};

// ======= Kho user (lưu file) =======
class UserRepository {
    // users.db: username|role|salt|hash|locked|failed_attempts
    unordered_map<string, UserRecord> db;
public:
    UserRepository() { load(); ensure_default_admin(); save(); }

    bool exists(const string& u) const { return db.count(u)>0; }
    optional<UserRecord> get(const string& u) const {
        auto it = db.find(u); if(it==db.end()) return nullopt; return it->second;
    }
    void upsert(const UserRecord& r) { db[r.username]=r; save(); }
    bool erase(const string& u) {
        if(db.erase(u)) { save(); return true; }
        return false;
    }
    vector<UserRecord> list_all() const {
        vector<UserRecord> v; v.reserve(db.size());
        for (auto &p: db) v.push_back(p.second);
        sort(v.begin(), v.end(), [](auto&a, auto&b){return a.username<b.username;});
        return v;
    }
private:
    void load() {
        db.clear();
        ifstream f("users.db");
        if (!f.good()) return;
        string line;
        while(getline(f,line)) {
            if(line.empty()) continue;
            // split by |
            vector<string> parts;
            string cur; stringstream ss(line);
            while(getline(ss,cur,'|')) parts.push_back(cur);
            if (parts.size() < 6) continue;
            UserRecord r;
            r.username = parts[0];
            r.role = str_to_role(parts[1]);
            r.salt = parts[2];
            r.hash = parts[3];
            r.locked = (parts[4]=="1");
            r.failed_attempts = stoi(parts[5]);
            db[r.username]=r;
        }
    }
    void save() const {
        ofstream f("users.db", ios::trunc);
        for (auto &p: db) {
            auto &r = p.second;
            f << r.username << "|"
              << role_to_str(r.role) << "|"
              << r.salt << "|"
              << r.hash << "|"
              << (r.locked?"1":"0") << "|"
              << r.failed_attempts
              << "\n";
        }
    }
    void ensure_default_admin() {
        if (!exists("admin")) {
            string salt = PasswordHasher::rand_hex();
            string hash = PasswordHasher::hash_pw("admin123", salt);
            UserRecord r{"admin", Role::ADMIN, salt, hash, false, 0};
            db[r.username]=r;
            Logger::warn("Tao admin mac dinh: admin/admin123");
        }
    }
};

// ======= Người dùng (đa hình) =======
class User {
protected:
    string username;
    Role role;
public:
    User(string u, Role r): username(std::move(u)), role(r){}
    virtual ~User() = default;
    string get_name() const { return username; }
    Role get_role() const { return role; }

    virtual void view_profile(const UserRepository& /*repo*/) {
        cout << "=== Ho so ===\n";
        cout << "Tai khoan : " << username << "\n";
        cout << "Vai tro   : " << role_to_str(role) << "\n";
    }
    virtual void menu(UserRepository& repo) = 0; // pure virtual
};

class NormalUser : public User {
public:
    using User::User;
    void change_password(UserRepository& repo) {
        cout << "Nhap mat khau hien tai: ";
        string oldp; getline(cin, oldp);
        cout << "Nhap mat khau moi: ";
        string newp; getline(cin, newp);
        auto rec = repo.get(username);
        if(!rec) { cout << "Loi: khong tim thay user.\n"; return; }
        string h = PasswordHasher::hash_pw(oldp, rec->salt);
        if (h != rec->hash) {
            cout << "Mat khau khong dung.\n";
            Logger::warn("Doi mat khau that bai cho user: " + username);
            return;
        }
        string salt = PasswordHasher::rand_hex();
        string hash = PasswordHasher::hash_pw(newp, salt);
        UserRecord r = *rec; r.salt = salt; r.hash = hash;
        repo.upsert(r);
        cout << "Doi mat khau thanh cong.\n";
        Logger::info("User doi mat khau: " + username);
    }
    void menu(UserRepository& repo) override {
        while(true){
            cout << "\n--- MENU USER ---\n";
            cout << "1. Xem ho so\n";
            cout << "2. Doi mat khau\n";
            cout << "0. Dang xuat\n";
            cout << "Chon: ";
            string ch; getline(cin, ch);
            if(ch=="1") { view_profile(repo); }
            else if(ch=="2") { change_password(repo); }
            else if(ch=="0") { break; }
            else cout << "Lua chon khong hop le.\n";
        }
    }
};

class Admin : public User {
public:
    using User::User;
    void list_users(UserRepository& repo) {
        auto v = repo.list_all();
        cout << left << setw(16) << "Username" << setw(8) << "Role" << setw(8) << "Lock" << setw(6) << "Fail" << "\n";
        for (auto &r : v) {
            cout << left << setw(16) << r.username
                 << setw(8) << role_to_str(r.role)
                 << setw(8) << (r.locked?"Yes":"No")
                 << setw(6) << r.failed_attempts
                 << "\n";
        }
    }
    void create_user(UserRepository& repo) {
        cout << "Nhap username moi: ";
        string u; getline(cin, u);
        if(u.empty()){ cout<<"Username rong!\n"; return; }
        if(repo.get(u)){ cout<<"Da ton tai.\n"; return; }
        cout << "Chon vai tro (1-Admin, 2-User): ";
        string rr; getline(cin, rr);
        Role r = (rr=="1")?Role::ADMIN:Role::USER;
        cout << "Nhap mat khau: ";
        string pw; getline(cin, pw);
        string salt = PasswordHasher::rand_hex();
        string hash = PasswordHasher::hash_pw(pw, salt);
        UserRecord rec{u, r, salt, hash, false, 0};
        repo.upsert(rec);
        cout << "Tao tai khoan thanh cong.\n";
        Logger::info("Admin tao tai khoan: " + u);
    }
    void reset_password(UserRepository& repo) {
        cout << "Nhap username: ";
        string u; getline(cin, u);
        auto rec = repo.get(u); if(!rec){ cout<<"Khong ton tai.\n"; return; }
        cout << "Nhap mat khau moi: ";
        string pw; getline(cin, pw);
        string salt = PasswordHasher::rand_hex();
        string hash = PasswordHasher::hash_pw(pw, salt);
        auto r = *rec; r.salt=salt; r.hash=hash; r.failed_attempts=0; r.locked=false;
        repo.upsert(r);
        cout << "Da dat lai mat khau & mo khoa (neu co).\n";
        Logger::warn("Admin reset password cho: " + u);
    }
    void lock_unlock(UserRepository& repo, bool lock) {
        cout << "Nhap username: ";
        string u; getline(cin, u);
        auto rec = repo.get(u); if(!rec){ cout<<"Khong ton tai.\n"; return; }
        auto r = *rec; r.locked=lock; if(!lock) r.failed_attempts=0;
        repo.upsert(r);
        cout << (lock?"Da khoa: ":"Da mo khoa: ") << u << "\n";
        Logger::warn(string("Admin ") + (lock?"lock ":"unlock ") + u);
    }
    void delete_user(UserRepository& repo) {
        cout << "Nhap username can xoa: ";
        string u; getline(cin, u);
        if(u=="admin"){ cout<<"Khong duoc xoa admin mac dinh.\n"; return; }
        if(repo.erase(u)) {
            cout<<"Da xoa.\n";
            Logger::warn("Admin xoa tai khoan: " + u);
        } else cout<<"Khong ton tai.\n";
    }
    void menu(UserRepository& repo) override {
        while(true){
            cout << "\n=== MENU ADMIN ===\n";
            cout << "1. Danh sach tai khoan\n";
            cout << "2. Tao tai khoan\n";
            cout << "3. Dat lai mat khau\n";
            cout << "4. Khoa tai khoan\n";
            cout << "5. Mo khoa tai khoan\n";
            cout << "6. Xoa tai khoan\n";
            cout << "7. Xem ho so\n";
            cout << "0. Dang xuat\n";
            cout << "Chon: ";
            string ch; getline(cin, ch);
            if(ch=="1") list_users(repo);
            else if(ch=="2") create_user(repo);
            else if(ch=="3") reset_password(repo);
            else if(ch=="4") lock_unlock(repo, true);
            else if(ch=="5") lock_unlock(repo, false);
            else if(ch=="6") delete_user(repo);
            else if(ch=="7") view_profile(repo);
            else if(ch=="0") break;
            else cout << "Lua chon khong hop le.\n";
        }
    }
};

// ======= Xác thực =======
class AuthService {
    UserRepository& repo;
    const int MAX_FAIL = 5;
public:
    explicit AuthService(UserRepository& r): repo(r) {}
    // trả về con trỏ đa hình (User hoặc Admin)
    unique_ptr<User> login() {
        cout << "Tai khoan: ";
        string u; getline(cin, u);
        cout << "Mat khau: ";
        string p; getline(cin, p);

        auto rec = repo.get(u);
        if(!rec) { cout<<"Sai tai khoan/Mat khau.\n"; Logger::warn("Login that bai (user khong ton tai): "+u); return nullptr; }
        if(rec->locked) { cout<<"Tai khoan dang bi khoa.\n"; Logger::warn("Login vao tai khoan bi khoa: "+u); return nullptr; }

        string h = PasswordHasher::hash_pw(p, rec->salt);
        if (h != rec->hash) {
            auto r = *rec; r.failed_attempts++;
            if(r.failed_attempts >= MAX_FAIL){ r.locked = true; Logger::warn("Tu dong khoa do nhap sai qua nhieu: "+u); }
            repo.upsert(r);
            cout<<"Sai tai khoan/Mat khau.\n";
            Logger::warn("Login that bai (sai mat khau): "+u);
            return nullptr;
        }
        // reset đếm sai
        auto r = *rec; r.failed_attempts = 0; repo.upsert(r);
        Logger::info("Dang nhap thanh cong: " + u);
        if(rec->role == Role::ADMIN) return make_unique<Admin>(u, Role::ADMIN);
        return make_unique<NormalUser>(u, Role::USER);
    }

    void signup() {
        cout << "Tao tai khoan USER moi\n";
        cout << "Username: ";
        string u; getline(cin, u);
        if(u.empty()){ cout<<"Khong duoc rong.\n"; return; }
        if(repo.get(u)){ cout<<"Da ton tai.\n"; return; }
        cout << "Mat khau: ";
        string p; getline(cin, p);
        string salt = PasswordHasher::rand_hex();
        string hash = PasswordHasher::hash_pw(p, salt);
        UserRecord rec{u, Role::USER, salt, hash, false, 0};
        repo.upsert(rec);
        cout << "Dang ky thanh cong. Ban co the dang nhap.\n";
        Logger::info("Dang ky user moi: " + u);
    }
};

// ======= Chương trình chính =======
int main() {

    UserRepository repo;
    AuthService auth(repo);

    cout << "==== Quan ly tai khoan & phan quyen (OOP) ====\n";
    cout << "Mac dinh co tai khoan admin/admin123\n\n";

    while(true){
        cout << "\n--- MENU CHINH ---\n";
        cout << "1. Dang nhap\n";
        cout << "2. Dang ky (User)\n";
        cout << "0. Thoat\n";
        cout << "Chon: ";
        string ch; getline(cin, ch);
        if(ch=="1") {
            if(auto u = auth.login()){
                u->menu(repo); // ĐA HÌNH: gọi menu tùy role
            }
        } else if(ch=="2") {
            auth.signup();
        } else if(ch=="0") {
            cout << "Tam biet!\n";
            break;
        } else {
            cout << "Khong hop le.\n";
        }
    }
    return 0;
}
