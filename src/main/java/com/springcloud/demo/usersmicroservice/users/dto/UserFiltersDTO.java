package com.springcloud.demo.usersmicroservice.users.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserFiltersDTO {
    @Positive
    Integer page;

    @Positive
    Integer limit;

    String q;
}
