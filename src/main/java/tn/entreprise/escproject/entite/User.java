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

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
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

    @NonNull
    @Enumerated(EnumType.STRING)
    private RoleUser roleUser;

    @NonNull
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