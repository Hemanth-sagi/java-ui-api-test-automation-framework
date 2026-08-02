package com.qa.framework.api.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A paginated page of users.
 *
 * <p>Modelling the envelope — not just the array — is what lets a test assert the pagination
 * contract itself: that {@code users.size()} honours {@code limit}, and that {@code skip} and
 * {@code total} come back as asked.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserListResponse {

    private List<User> users;
    private Integer total;
    private Integer skip;
    private Integer limit;
}
