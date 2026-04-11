@echo off
rem Sửa lỗi mất đường dẫn đến PowerShell trên máy của bạn
set "PATH=%PATH%;C:\Windows\System32\WindowsPowerShell\v1.0;C:\Windows\System32"

echo Dang doc cau hinh tu file .env...
if exist .env (
    FOR /F "usebackq eol=# tokens=1,* delims==" %%A IN (".env") DO (
        set "%%A=%%B"
    )
    echo Da nap xong bien moi truong!
) else (
    echo Khong tim thay file .env. Co the se bao loi!
)

cd backend
echo Dang khoi dong Backend...
call mvnw.cmd spring-boot:run
