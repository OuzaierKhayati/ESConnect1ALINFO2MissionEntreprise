package tn.entreprise.escproject.entite;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
    @Entity
    @Table(name = "publications")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Publication {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank(message = "Le contenu ne peut pas etre vide")
        @Column(columnDefinition = "TEXT", nullable = false)
        private String contenu;

        @Column(name = "image_url")
        private String imageUrl;

        @Column(name = "date_creation", nullable = false)
        private LocalDateTime dateCreation;

        @Column(name = "date_modification")
        private LocalDateTime dateModification;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "auteur_id", nullable = false)
        private User auteur;

        @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        @Builder.Default
        private List<Commentaire> commentaires = new ArrayList<>();

        @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        @Builder.Default
        private List<Like> likes = new ArrayList<>();

        @PrePersist
        protected void onCreate() {
            this.dateCreation = LocalDateTime.now();
        }

        @PreUpdate
        protected void onUpdate() {
            this.dateModification = LocalDateTime.now();
        }

        @Transient
        public int getNombreLikes() {
            return likes != null ? likes.size() : 0;
        }

        @Transient
        public int getNombreCommentaires() {
            return commentaires != null ? commentaires.size() : 0;
        }
    }
