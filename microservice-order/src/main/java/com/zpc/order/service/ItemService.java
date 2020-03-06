package com.zpc.order.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.zpc.order.entity.Item;
import com.zpc.order.feign.ItemFeignClient;
import com.zpc.order.properties.OrderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ItemService {

    // Spring框架对RESTful方式的http请求做了封装，来简化操作
    @Autowired
    private RestTemplate restTemplate;

//    @Value("${myspcloud.item.url}")
//    private String itemUrl;

    @Autowired
    private OrderProperties orderProperties;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private ItemFeignClient itemFeignClient;

    /**
     * 进行容错处理
     * fallbackMethod的方法参数个数类型要和原方法一致
     *
     * @param id
     * @return
     */
    @HystrixCommand(fallbackMethod = "queryItemByIdFallbackMethod")
    public Item queryItemById(Long id) {
        String serviceId = "app-item";
        List<ServiceInstance> instances = this.discoveryClient.getInstances(serviceId);
//        return this.restTemplate.getForObject("http://127.0.0.1:8081/item/"
//                + id, Item.class);
//        return this.restTemplate.getForObject(itemUrl
//                + id, Item.class);

//        return this.restTemplate.getForObject(orderProperties.getItem().getUrl()
//                + id, Item.class);


        String itemUrl = "http://app-item/item/{id}";
        Item result = restTemplate.getForObject(itemUrl, Item.class, id);

        System.out.println("订单系统调用商品服务,result:" + result);
        return result;
    }


    /**
     * 请求失败执行的方法
     * fallbackMethod的方法参数个数类型要和原方法一致
     *
     * @param id
     * @return
     */
    public Item queryItemByIdFallbackMethod(Long id) {
        return new Item(id, "查询商品信息出错!", null, null, null);
    }


//    @HystrixCommand(fallbackMethod = "queryItemByIdFallbackMethod")
    public Item queryItemById3(Long id) {
//        String itemUrl = "http://app-item/item/{id}";
        Item result = itemFeignClient.queryItemById(id);
        System.out.println("===========HystrixCommand queryItemById-线程池名称：" + Thread.currentThread().getName() + "订单系统调用商品服务,result:" + result);
        return result;
    }

}
