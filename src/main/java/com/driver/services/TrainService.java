package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import io.swagger.models.AbstractModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();
        train.setBookedTickets(new ArrayList<>());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        String rout = "";
        for(Station station:trainEntryDto.getStationRoute()){
            rout += station.name() + ",";
        }
        rout = rout.substring(0,rout.length()-1);
        train.setRoute(rout);
        train = trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        String Root[]=train.getRoute().split(",");

        HashMap<String,Integer> map = new HashMap<>();
        for(int i=0;i<Root.length;i++){
            map.put(Root[i],i);
        }
        if(!map.containsKey(seatAvailabilityEntryDto.getToStation().toString()) || !map.containsKey(seatAvailabilityEntryDto.getFromStation().toString())){
            return 0;
        }

        int bookedSeats = 0;
        List<Ticket> ticketList = train.getBookedTickets();
        for(Ticket ticket:ticketList){
            bookedSeats += ticket.getPassengersList().size();
        }

        int available = train.getNoOfSeats() - bookedSeats;
        for(Ticket t:ticketList){
            String from  = t.getFromStation().toString();
            String to = t.getToStation().toString();
            if(map.get(seatAvailabilityEntryDto.getToStation().toString())<=map.get(from)){
                available++;
            }
            else if (map.get(seatAvailabilityEntryDto.getFromStation().toString())>=map.get(to)){
                available++;
            }
        }
        return  available+2;

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.


    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train = trainRepository.findById(trainId).get();

       String stations[] = train.getRoute().split(",");
        boolean f = false;
        for(int i=0;i<stations.length;i++){
            if(stations[i].toString().equals(station)){
                f = true;
                break;
            }
        }

        if(!f){
            throw new Exception("Train is not passing from this station");
        }
        List<Ticket> ticketList = train.getBookedTickets();
        int ans = 0;
        for(Ticket t:ticketList){
            String from = t.getFromStation().toString();
            if(from.equals(station)){
                ans += t.getPassengersList().size();
            }
        }
        return ans;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId) throws Exception {

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train = trainRepository.findById(trainId).get();


        List<Ticket> ticketList = train.getBookedTickets();
        int oldAge = 0;
        for(Ticket t:ticketList){
            List<Passenger> passengers = t.getPassengersList();
            for(Passenger passenger:passengers){
                oldAge = Math.max(oldAge,passenger.getAge());
            }
        }

        return oldAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Integer> TrainList = new ArrayList<>();
        List<Train> trains = trainRepository.findAll();
        for(Train t:trains){
            String s = t.getRoute();
            String[] ans = s.split(",");
            for(int i=0;i<ans.length;i++){
                if(Objects.equals(ans[i], String.valueOf(station))){
                    int startTimeInMin = (startTime.getHour() * 60) + startTime.getMinute();
                    int lastTimeInMin = (endTime.getHour() * 60) + endTime.getMinute();


                    int departureTimeInMin = (t.getDepartureTime().getHour() * 60) + t.getDepartureTime().getMinute();
                    int reachingTimeInMin  = departureTimeInMin + (i * 60);
                    if(reachingTimeInMin>=startTimeInMin && reachingTimeInMin<=lastTimeInMin)
                        TrainList.add(t.getTrainId());
                }
            }
        }
        return TrainList;
        
    }

}
