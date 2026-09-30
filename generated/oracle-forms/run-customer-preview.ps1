$ErrorActionPreference = "Stop"

$oracleHome = "C:\app\Administrator\product\26ai\dbhomeFree"
$formsHome = "C:\Oracle\FormsRuntime14c"
$javaCompiler = (Get-Command javac.exe -ErrorAction Stop).Source
$javaRuntime = (Get-Command java.exe -ErrorAction Stop).Source
$sourceFile = Join-Path $PSScriptRoot "CustomerPreviewServer.java"
$htmlFile = Join-Path $PSScriptRoot "customer-preview.html"
$classesDirectory = Join-Path $env:TEMP "customer-preview-java"
$libraries = @(
    (Join-Path $oracleHome "jdbc\lib\ojdbc8.jar"),
    (Join-Path $formsHome "oracle_common\modules\thirdparty\jackson-databind-2.17.0.jar"),
    (Join-Path $formsHome "oracle_common\modules\thirdparty\jackson-core-2.17.0.jar"),
    (Join-Path $formsHome "oracle_common\modules\thirdparty\jackson-annotations-2.17.0.jar")
)

foreach ($path in @($sourceFile, $htmlFile) + $libraries) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Required file not found: $path"
    }
}

New-Item -ItemType Directory -Path $classesDirectory -Force | Out-Null
$classpath = $libraries -join ";"
& $javaCompiler --add-modules jdk.httpserver -encoding UTF-8 -cp $classpath -d $classesDirectory $sourceFile
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed with exit code $LASTEXITCODE."
}

$securePassword = $null
$passwordPointer = [IntPtr]::Zero
$previousPassword = $env:CUSTOMER_DB_PASSWORD
$previousUrl = $env:CUSTOMER_DB_URL
$previousUser = $env:CUSTOMER_DB_USER
try {
    if ([string]::IsNullOrWhiteSpace($env:CUSTOMER_DB_URL)) {
        $defaultDatabaseUrl = "jdbc:oracle:thin:@//127.0.0.1:1521/FREEPDB1"
        $databaseUrl = Read-Host "URL JDBC [$defaultDatabaseUrl]"
        $env:CUSTOMER_DB_URL = if ([string]::IsNullOrWhiteSpace($databaseUrl)) { $defaultDatabaseUrl } else { $databaseUrl }
    }
    $securePassword = Read-Host "Mot de passe Oracle de CUSTOMER_APP" -AsSecureString
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $env:CUSTOMER_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    & $javaRuntime --add-modules jdk.httpserver -cp "$classesDirectory;$classpath" CustomerPreviewServer $htmlFile
    if ($LASTEXITCODE -ne 0) {
        throw "Customer preview server exited with code $LASTEXITCODE."
    }
}
finally {
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    if ($null -ne $securePassword) {
        $securePassword.Dispose()
    }
    if ($null -eq $previousPassword) {
        Remove-Item Env:CUSTOMER_DB_PASSWORD -ErrorAction SilentlyContinue
    }
    else {
        $env:CUSTOMER_DB_PASSWORD = $previousPassword
    }
    if ($null -eq $previousUrl) {
        Remove-Item Env:CUSTOMER_DB_URL -ErrorAction SilentlyContinue
    }
    else {
        $env:CUSTOMER_DB_URL = $previousUrl
    }
    if ($null -eq $previousUser) {
        Remove-Item Env:CUSTOMER_DB_USER -ErrorAction SilentlyContinue
    }
    else {
        $env:CUSTOMER_DB_USER = $previousUser
    }
}