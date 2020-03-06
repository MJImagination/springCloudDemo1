package com.zpc.order.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.zpc.order.entity.Order;
import com.zpc.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;


    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @GetMapping(value = "order/{orderId}")
    public Order queryOrderById(@PathVariable("orderId") String orderId) {
        System.out.println("order");
//        return this.orderService.queryOrderById(orderId);
        return this.orderService.queryOrderById3(orderId);
    }


    @RequestMapping("/consumer")
    @HystrixCommand(fallbackMethod = "helloConsumerFallbackMethod")
    public String helloConsumer() throws ExecutionException, InterruptedException {
        //这里是根据配置文件的那个providers属性取的
//        ServiceInstance serviceInstance = loadBalancerClient.choose("providers");
        String serviceId = "app-item";
        List<URI> uriS = new ArrayList<>();
        for(int i=0;i<100;i++){
            ServiceInstance serviceInstance = this.loadBalancerClient.choose(serviceId);
            //负载均衡算法默认是轮询，轮询取得服务
            URI uri = URI.create(String.format("http://%s:%s", serviceInstance.getHost(), serviceInstance.getPort()));
            uriS.add(uri);
        }

        return JSON.toJSONString(uriS);
    }
}
