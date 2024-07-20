package com.sky.controller.user;

import cn.hutool.db.Page;
import com.alibaba.fastjson.JSON;
import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.scene.input.DataFormat.URL;

/**
 * @author 吴勇华
 * @description: TODO
 */

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户端订单相关接口")
public class OrderController {

    @Resource
    private OrderService orderService;
    @Resource
    private WebSocketServer webSocketServer;
    @Resource
    private OrderMapper orderMapper;
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO)  {
        log.info("用户下单，参数为：{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 模拟微信订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = OrderPaymentVO.builder()
                .nonceStr("1670380960")
                .packageStr("prepay_id=wx07104240042328")
                .paySign("qwr234e23123")
                .signType("RSA")
                .timeStamp("4235123123123")
                .build();
        log.info("生成预支付交易单：{}", orderPaymentVO);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(WeChatPayUtil.JSAPI);
        httpClient.execute(httpGet);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /*
     * 查询订单详情
     * @return: Result<OrderVO>
     **/
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        log.info("当前需要查询的订单号：{}", id);
        OrderVO orderVO = orderService.getOrderDetailById(id);
        return Result.success(orderVO);
    }
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("分页查询信息：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.queryOrdersByPage(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id) throws Exception {
        orderService.cancelOrder(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        log.info("当前需要再来一单的订单号为：{}", id);
        orderService.repetition(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable Long id){
        log.info("当前需要再来一单的订单号为：{}", id);
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 通过 websocket 向客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号 ： " + orders.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
        return Result.success();
    }
}
