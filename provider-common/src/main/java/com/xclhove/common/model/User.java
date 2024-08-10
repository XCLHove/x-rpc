package com.xclhove.common.model;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author xclhove
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class User implements Serializable {
    private Integer id;
    private String name;
}
