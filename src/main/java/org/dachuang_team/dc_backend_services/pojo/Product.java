package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;
import org.apache.catalina.User;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String productName;
    private String title;
    private Double price;
    private String category;
    private String origin;
    private String imageUrl;
    private Boolean approved;
    private LocalDateTime publishedAt;

//    @ManyToOne
//    @JoinColumn(name = "seller_id")
//    private User seller;
//
//    @OneToMany(mappedBy = "product")
//    private List<Order_Item> orderItems;
}
