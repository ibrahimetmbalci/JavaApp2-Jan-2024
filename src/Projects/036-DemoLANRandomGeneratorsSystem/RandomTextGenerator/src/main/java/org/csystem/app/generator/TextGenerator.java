package org.csystem.app.generator;

import com.karandev.io.util.console.Console;
import com.karandev.util.net.TcpUtil;
import com.karandev.util.net.exception.NetworkException;
import org.csystem.util.string.StringUtil;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.csystem.app.generator.global.ServerUtil.*;

public class TextGenerator {
    private final RandomGenerator m_randomGenerator = new Random();


    private static void setInActive(ServerInfo serverInfo)
    {
        synchronized (SYNC_LOCK) {
            serverInfo.setActive(false);
        }
    }

    private void sendTextAsyncCallback(ServerInfo serverInfo, String text)
    {
        try (var socket = new Socket(serverInfo.getHost(), serverInfo.getPort())) {
            TcpUtil.sendLine(socket, text);
        }
        catch (NetworkException ex) {
            Console.Error.writeLine("Network Error occurred:%s", ex.getMessage());
            setInActive(serverInfo);
        }
        catch (IOException ex) {
            Console.Error.writeLine("IO Error occurred:%s", ex.getMessage());
            setInActive(serverInfo);
        }
        catch (Throwable ex) {
            Console.Error.writeLine("Error occurred:%s", ex.getMessage());
            setInActive(serverInfo);
        }
    }

    public void run()
    {
        var text = StringUtil.getRandomTextEN(m_randomGenerator, m_randomGenerator.nextInt(5, 15));

        Console.writeLine("Generated text:%s", text);

        synchronized (SYNC_LOCK) {
            SERVERS.stream()
                    .filter(ServerInfo::isActive)
                    .forEach(si -> THREAD_POOL.execute(() -> sendTextAsyncCallback(si, text)));
        }
    }
}
