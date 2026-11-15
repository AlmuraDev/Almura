# Maximizes the 4.0 dev client's window once it appears. Started (hidden) by Gradle's prepareClientRun,
# which runs before both ./gradlew runClient and the IDE's "Almura Client" run. Gives up after two minutes.
Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class AlmuraWindow {
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
}
'@

$started = Get-Date
$deadline = $started.AddMinutes(2)
while ((Get-Date) -lt $deadline) {
    # Only a java started after this script, so an already open client (or the 3.5 one) is left alone
    $client = Get-Process java, javaw -ErrorAction SilentlyContinue | Where-Object {
        $_.MainWindowHandle -ne [IntPtr]::Zero -and
        $_.MainWindowTitle -like 'Minecraft*' -and
        $_.MainWindowTitle -notlike 'Minecraft 1.12*' -and
        $_.StartTime -ge $started.AddSeconds(-5)
    } | Select-Object -First 1
    if ($client) {
        # 3 = SW_SHOWMAXIMIZED
        [AlmuraWindow]::ShowWindow($client.MainWindowHandle, 3) | Out-Null
        exit 0
    }
    Start-Sleep -Milliseconds 500
}
