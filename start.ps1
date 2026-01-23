# Kill any process using port 8080
$port = 8080
$connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue

if ($connections) {
    $processIds = $connections | Select-Object -ExpandProperty OwningProcess -Unique

    foreach ($pid in $processIds) {
        $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "Stopping process: $($process.ProcessName) (PID: $pid)"
            Stop-Process -Id $pid -Force
            Write-Host "Process stopped successfully"
        }
    }
} else {
    Write-Host "No process is using port $port"
}

# Wait a moment for the port to be released
Start-Sleep -Seconds 2

# Run the application
Write-Host "`nStarting application..."
.\mvnw.cmd spring-boot:run
