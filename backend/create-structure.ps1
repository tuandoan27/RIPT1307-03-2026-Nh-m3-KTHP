# =====================================================
# Script tao folder structure cho Spring Boot project
# Base package: com.borrowapp
# Chay trong thu muc goc cua project (ngang voi src/)
# =====================================================

$base = "src\main\java\com\borrowapp"

$folders = @(
    # common
    "$base\common\config"
    "$base\common\constants"
    "$base\common\exception"
    "$base\common\response"
    "$base\common\utils"
    "$base\common\middleware"

    # auth
    "$base\auth\controller"
    "$base\auth\dto"
    "$base\auth\service"
    "$base\auth\util"

    # user
    "$base\user\controller"
    "$base\user\dto"
    "$base\user\entity"
    "$base\user\repository"
    "$base\user\service"

    # equipment
    "$base\equipment\controller"
    "$base\equipment\dto"
    "$base\equipment\entity"
    "$base\equipment\repository"
    "$base\equipment\service"

    # request
    "$base\request\controller"
    "$base\request\dto"
    "$base\request\entity"
    "$base\request\repository"
    "$base\request\service"
    "$base\request\scheduler"

    # notification
    "$base\notification\controller"
    "$base\notification\dto"
    "$base\notification\entity"
    "$base\notification\repository"
    "$base\notification\service"

    # activity
    "$base\activity\controller"
    "$base\activity\dto"
    "$base\activity\entity"
    "$base\activity\repository"
    "$base\activity\service"
)

foreach ($folder in $folders) {
    New-Item -ItemType Directory -Force -Path $folder | Out-Null
    New-Item -ItemType File -Force -Path "$folder\.gitkeep" | Out-Null
    Write-Host "Created: $folder"
}

Write-Host ""
Write-Host "Done! Folder structure da duoc tao thanh cong."
Write-Host "Mo IntelliJ va refresh lai project la thay ngay."