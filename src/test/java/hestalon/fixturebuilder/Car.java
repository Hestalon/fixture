package hestalon.fixturebuilder;

import hestalon.fixturebuilder.subpackage.Passenger;

public record Car(
    String name,
    Integer age,
    Passenger passenger
) {
}
