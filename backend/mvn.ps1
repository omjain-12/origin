$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\omjai\.maven\apache-maven-3.9.9\bin;$env:PATH"

$envFile = Join-Path $PSScriptRoot "..\.env"
if (-not (Test-Path $envFile)) {
    $envFile = Join-Path $PSScriptRoot ".env"
}
if (Test-Path $envFile) {
    Get-Content $envFile | Where-Object { $_ -match '^\s*([^#=]+)=(.*)$' } | ForEach-Object {
        $k = $matches[1].Trim()
        $v = $matches[2].Trim()
        if ($k) {
            [System.Environment]::SetEnvironmentVariable($k, $v, "Process")
        }
    }
}

& "C:\Users\omjai\.maven\apache-maven-3.9.9\bin\mvn.cmd" @args
