package org.apache.flink.runtime.e2data.handler;

import org.apache.flink.runtime.rest.handler.router.RouteResult;
import org.apache.flink.runtime.rest.handler.router.RoutedRequest;
import org.apache.flink.runtime.rest.handler.router.Router;

import org.apache.flink.shaded.netty4.io.netty.buffer.Unpooled;
import org.apache.flink.shaded.netty4.io.netty.channel.ChannelHandler;
import org.apache.flink.shaded.netty4.io.netty.channel.ChannelHandlerContext;
import org.apache.flink.shaded.netty4.io.netty.channel.SimpleChannelInboundHandler;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.DefaultFullHttpResponse;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.FullHttpResponse;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpVersion;
import org.apache.flink.shaded.netty4.io.netty.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Scanner;

/**
 * Handles Flink cluster lifecycle.
 */
@ChannelHandler.Sharable
public class StartFlinkHandler extends SimpleChannelInboundHandler<RoutedRequest> {

	private static final String COMMAND = "command";
	private static final String PATH = "/Users/christos/Projects/flink/flink-dist/target/flink-1.12-SNAPSHOT-bin/flink-1.12-SNAPSHOT/bin/";

	/** Default logger, if none is specified. */
	private static final Logger LOG = LoggerFactory.getLogger(StartFlinkHandler.class);

	private Router router;

	public StartFlinkHandler(Router router) {
		this.router = router;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, RoutedRequest routedRequest) throws Exception {
		LOG.error("Read RoutedRequest");

		RouteResult<String> routeResult = router.route(routedRequest.getRequest().method(), routedRequest.getRequest().uri());
		Map<String, String> params = routeResult.pathParams();

		String command = params.getOrDefault(COMMAND, "");
		if (!command.isEmpty()) {
			try {
				LOG.error("Command to run: " + command);

				ProcessBuilder processBuilder = new ProcessBuilder(
					"bash",
					PATH + "e2datamanager.sh",
					command);

				processBuilder.inheritIO();
				Process process = processBuilder.start();
				Scanner scanner = new Scanner(process.getInputStream()).useDelimiter("\\A");
				LOG.error(scanner.next());
				process.waitFor();
			} finally {
				returnResponse(channelHandlerContext, routedRequest, routeResult);
			}
		}
		else {
			returnResponse(channelHandlerContext, routedRequest, routeResult);
		}
	}

	private void returnResponse(ChannelHandlerContext channelHandlerContext, RoutedRequest routedRequest, RouteResult routeResult) {
		String content =
			"router: \n" + router + "\n" +
				"req: " + routedRequest + "\n\n" +
				"routeResult: \n" +
				"pathParams: " + routeResult.pathParams() + "\n" +
				"queryParams: " + routeResult.queryParams() + "\n\n" +
				"allowedMethods: " + router.allowedMethods(routedRequest.getRequest().uri());

		FullHttpResponse res = new DefaultFullHttpResponse(
			HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
			Unpooled.copiedBuffer(content, CharsetUtil.UTF_8)
		);

		res.headers().set(HttpHeaderNames.CONTENT_TYPE,   "text/plain");
		res.headers().set(HttpHeaderNames.CONTENT_LENGTH, res.content().readableBytes());

		channelHandlerContext.writeAndFlush(res);
	}

}
