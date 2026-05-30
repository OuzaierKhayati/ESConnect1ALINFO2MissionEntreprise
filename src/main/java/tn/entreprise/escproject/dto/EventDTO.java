package tn.entreprise.escproject.dto;

import java.time.LocalDateTime;

public class EventDTO {
    private Long id;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String localisation;
    private String imageUrl;
    private Long createurId;
    private String createurFirstName;
    private String createurLastName;
    private String createurEmail;
    private Integer nombreParticipants;
    private String statut;
    private Boolean isPublic;
    private LocalDateTime dateCreation;

    public EventDTO() {
    }

    public EventDTO(Long id, String titre, String description, LocalDateTime dateDebut,
                    LocalDateTime dateFin, String localisation, String imageUrl,
                    Long createurId, Integer nombreParticipants, String statut) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.localisation = localisation;
        this.imageUrl = imageUrl;
        this.createurId = createurId;
        this.nombreParticipants = nombreParticipants;
        this.statut = statut;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getCreateurId() {
        return createurId;
    }

    public void setCreateurId(Long createurId) {
        this.createurId = createurId;
    }

    public String getCreateurFirstName() {
        return createurFirstName;
    }

    public void setCreateurFirstName(String createurFirstName) {
        this.createurFirstName = createurFirstName;
    }

    public String getCreateurLastName() {
        return createurLastName;
    }

    public void setCreateurLastName(String createurLastName) {
        this.createurLastName = createurLastName;
    }

    public String getCreateurEmail() {
        return createurEmail;
    }

    public void setCreateurEmail(String createurEmail) {
        this.createurEmail = createurEmail;
    }

    public Integer getNombreParticipants() {
        return nombreParticipants;
    }

    public void setNombreParticipants(Integer nombreParticipants) {
        this.nombreParticipants = nombreParticipants;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
