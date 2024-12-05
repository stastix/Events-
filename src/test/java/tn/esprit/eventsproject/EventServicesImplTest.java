package tn.esprit.eventsproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import tn.esprit.eventsproject.services.EventServicesImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServicesImplTest {

    @InjectMocks
    private EventServicesImpl eventServices;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addParticipant_shouldSaveParticipant() {
        Participant participant = new Participant();
        participant.setIdPart(1);

        when(participantRepository.save(participant)).thenReturn(participant);

        Participant savedParticipant = eventServices.addParticipant(participant);

        assertNotNull(savedParticipant);
        assertEquals(1, savedParticipant.getIdPart());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void addAffectEvenParticipant_withId_shouldAffectParticipantToEvent() {
        Participant participant = new Participant();
        participant.setIdPart(1);

        Event event = new Event();
        event.setIdEvent(10);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event updatedEvent = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(updatedEvent);
        assertTrue(participant.getEvents().contains(event));
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void addAffectEvenParticipant_shouldAffectAllParticipantsToEvent() {
        Event event = new Event();
        event.setIdEvent(10);

        Participant participant = new Participant();
        participant.setIdPart(1);

        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        event.setParticipants(participants);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event updatedEvent = eventServices.addAffectEvenParticipant(event);

        assertNotNull(updatedEvent);
        assertTrue(participant.getEvents().contains(event));
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void addAffectLog_shouldAffectLogisticsToEvent() {
        Logistics logistics = new Logistics();
        logistics.setIdLog(1);

        Event event = new Event();
        event.setDescription("Test Event");

        when(eventRepository.findByDescription("Test Event")).thenReturn(event);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics savedLogistics = eventServices.addAffectLog(logistics, "Test Event");

        assertNotNull(savedLogistics);
        assertTrue(event.getLogistics().contains(logistics));
        verify(logisticsRepository, times(1)).save(logistics);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void getLogisticsDates_shouldReturnLogisticsWithinDateRange() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        Event event = new Event();
        Logistics logistics = new Logistics();
        logistics.setReserve(true);
        logistics.setPrixUnit(10.0f);
        logistics.setQuantite(2);

        event.setLogistics(new HashSet<>(Collections.singletonList(logistics)));

        when(eventRepository.findByDateDebutBetween(startDate, endDate)).thenReturn(Collections.singletonList(event));

        List<Logistics> logisticsList = eventServices.getLogisticsDates(startDate, endDate);

        assertNotNull(logisticsList);
        assertEquals(1, logisticsList.size());
        assertTrue(logisticsList.contains(logistics));
    }

    @Test
    void calculCout_shouldCalculateAndSaveCost() {
        Event event = new Event();
        event.setDescription("Test Event");

        Logistics logistics = new Logistics();
        logistics.setReserve(true);
        logistics.setPrixUnit(50.0f);
        logistics.setQuantite(2);

        event.setLogistics(new HashSet<>(Collections.singletonList(logistics)));

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi", "Ahmed",
                Tache.ORGANISATEUR))
                .thenReturn(Collections.singletonList(event));

        eventServices.calculCout();

        assertEquals(100.0f, event.getCout());
        verify(eventRepository, times(1)).save(event);
    }
}
