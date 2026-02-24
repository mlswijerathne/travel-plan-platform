try {
    $c = New-Object System.Net.Sockets.TcpClient('aws-0-ap-northeast-1.pooler.supabase.com', 5432)
    Write-Host "TCP connected"
    $stream = $c.GetStream()
    $stream.ReadTimeout = 10000

    # Send PostgreSQL SSLRequest message
    $sslRequest = [byte[]]@(0,0,0,8,4,210,22,47)
    $stream.Write($sslRequest, 0, 8)
    $stream.Flush()

    $response = $stream.ReadByte()
    if ($response -eq 83) {
        Write-Host "Server supports SSL (responded 'S')"
    } elseif ($response -eq 78) {
        Write-Host "Server does NOT support SSL (responded 'N')"
    } else {
        Write-Host "Unexpected response: $response"
    }
    $c.Close()
} catch {
    Write-Host ("Error: " + $_.Exception.Message)
}
