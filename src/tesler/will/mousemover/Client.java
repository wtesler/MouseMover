package tesler.will.mousemover;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

public class Client {

	Context context;
	String host;
	int port;
	int len;
	Socket socket = new Socket();
	byte buf[] = new byte[8];
	boolean run;
	boolean newData = false;
	int x, y;

	Client(Context cont, String host, int port) {
		context = cont;
		this.host = host;
		this.port = port;

	}

	void start() {
		run = true;
		ClientAsync ca = new ClientAsync();
		ca.execute();
	}

	void stop() {
		run = false;
	}

	void send(int x, int y) {
		if (newData == false) {
			this.x = x;
			this.y = y;
			newData = true;
		}
	}

	class ClientAsync extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				/**
				 * Create a client socket with the host, port, and timeout
				 * information.
				 */
				socket.bind(null);
				socket.connect((new InetSocketAddress(host, port)), 500);

				/**
				 * Create a byte stream from a JPEG file and pipe it to the
				 * output stream of the socket. This data will be retrieved by
				 * the server device.
				 */
				OutputStream outputStream = socket.getOutputStream();
				ContentResolver cr = context.getContentResolver();
				InputStream inputStream = null;
				inputStream = cr.openInputStream(Uri
						.parse("path/to/picture.jpg"));
				while ((len = inputStream.read(buf)) != -1) {
					outputStream.write(buf, 0, len);
				}
				while (run) {
					if (newData) {
						byte[] xbytes = ByteBuffer.allocate(4).putInt(x)
								.array();
						byte[] ybytes = ByteBuffer.allocate(4).putInt(y)
								.array();
						int i = 0;
						for (; i < 4; i++) {
							buf[i] = xbytes[i];
						}
						for (; i < 8; i++) {
							buf[i] = ybytes[i - 4];
						}
						outputStream.write(buf, 0, 8);
						newData = false;
					}
				}
				outputStream.close();
				inputStream.close();
			} catch (FileNotFoundException e) {
				// catch logic
			} catch (IOException e) {
				// catch logic
			}

			/**
			 * Clean up any open sockets when done transferring or if an
			 * exception occurred.
			 */
			finally {
				if (socket != null) {
					if (socket.isConnected()) {
						try {
							socket.close();
						} catch (IOException e) {
							// catch logic
						}
					}
				}
			}
			return null;
		}
	}

}
