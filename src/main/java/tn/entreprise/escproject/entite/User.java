//package tn.entreprise.escproject.entite;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import lombok.*;
//
//import java.util.List;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Table(name = "users")
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotBlank
//    private String firstName;
//
//    @NotBlank
//    private String lastName;
//
//    @Email
//    @Column(unique = true)
//    private String email;
//
//    @NotBlank
//    private String password;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "sender")
//    private List<Connection> sentConnections;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "receiver")
//    private List<Connection> receivedConnections;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "sender")
//    private List<Message> sentMessages;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "receiver")
//    private List<Message> receivedMessages;
//}

package tn.entreprise.escproject.entite;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private RoleUser roleUser;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @JsonIgnore
    @OneToMany(mappedBy = "sender")
    private List<Connection> sentConnections;

    @JsonIgnore
    @OneToMany(mappedBy = "receiver")
    private List<Connection> receivedConnections;

    @JsonIgnore
    @OneToMany(mappedBy = "sender")
    private List<Message> sentMessages;

    @JsonIgnore
    @OneToMany(mappedBy = "receiver")
    private List<Message> receivedMessages;
}