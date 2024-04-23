package io.github.lhtforit.lhtregistry.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Leo
 * @date 2024/04/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"url"})
public class Server {

    private String url;
    private boolean status;
    private boolean leader;
    private long version;

}
