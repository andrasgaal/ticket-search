class PriceDiagram extends React.Component {
    constructor() {
        super();
        let todayIso = new Date().toISOString();
        let today = todayIso.substring(0, todayIso.indexOf("T"));
        this.state = {
            iatas: [],
            departureStation: "BUD",
            arrivalStation: "LTN",
            flightDate: today,
            outboundFlights: {},
            inboundFlights: {}
        };
        this.handleDepartureStationChange = this.handleDepartureStationChange.bind(this);
        this.handleArrivalStationChange = this.handleArrivalStationChange.bind(this);
        this.handleFlightDateChange = this.handleFlightDateChange.bind(this);
        this.updateFlights = this.updateFlights.bind(this);
    }

    componentDidMount() {
        this.getIatas();
        this.updateFlights();
    }

    getIatas() {
        let self = this;
        $.ajax({
            url: "http://localhost:8080/iatas"
        }).done(function (data) {
            self.setState({iatas: data});
        }).fail(function () {
            self.setState({iatas: []});
        })
    }

    updateFlights() {
        let self = this;
        $.ajax({
            url: "http://localhost:8080/flights/groupby/searchdate?departureStation=" + this.state.departureStation
            + "&arrivalStation=" + this.state.arrivalStation + "&flightDate=" + this.state.flightDate
        }).done(function (flights) {
            self.setState({"outboundFlights": flights});
        }).fail(function () {
            self.setState({"outboundFlights": {}});
        });

        $.ajax({
            url: "http://localhost:8080/flights/groupby/searchdate?departureStation=" + this.state.arrivalStation
            + "&arrivalStation=" + this.state.departureStation + "&flightDate=" + this.state.flightDate
        }).done(function (flights) {
            self.setState({"inboundFlights": flights});
        }).fail(function () {
            self.setState({"inboundFlights": {}});
        });
    }

    handleDepartureStationChange(iata) {
        this.state.departureStation = iata;
        this.updateFlights();
    }

    handleArrivalStationChange(iata) {
        this.state.arrivalStation = iata;
        this.updateFlights();
    }

    handleFlightDateChange(date) {
        this.state.flightDate = date;
        this.updateFlights();
    }

    render() {
        return (
            <div>
                <div className="block">
                    <p className="title">Stations</p>
                    <Dropdown iatas={this.state.iatas} station={"departure"} chosenIata={this.state.departureStation}
                              labelText={"From"} onStationChange={this.handleDepartureStationChange}/>
                    {/*<div className="separator"/>*/}
                    <Dropdown iatas={this.state.iatas} station={"arrival"} chosenIata={this.state.arrivalStation}
                              labelText={"To"} onStationChange={this.handleArrivalStationChange}/>
                    <div className="separator"/>
                    <DatePicker flightDate={this.state.flightDate} onFlightDateChange={this.handleFlightDateChange}/>
                    <div className="separator"/>
                    <UpdateButton onClick={this.updateFlights}/>
                </div>
                <div className="block">
                    <Diagram departureStation={this.state.departureStation} arrivalStation={this.state.arrivalStation} flights={this.state.outboundFlights}/>
                </div>
                <div className="block">
                    <Diagram departureStation={this.state.arrivalStation} arrivalStation={this.state.departureStation} flights={this.state.inboundFlights}/>
                </div>
            </div>
        );
    }
}

class Dropdown extends React.Component {
    constructor(props) {
        super(props);
    }

    handleIataChange(iata) {
        this.props.onStationChange(iata)
    }

    render() {
        let station = this.props.station;

        let self = this;
        let dropdownMenuElements = [];
        this.props.iatas.forEach(function (iata) {
            dropdownMenuElements.push(<DropdownMenuElement key={iata} iata={iata}
                                                           onClick={() => self.handleIataChange(iata)}/>);
        });

        return (
            <div className="dropdown-wrapper">
                {/*<label htmlFor={station + "-station-dropdown-button"}>{this.props.labelText}</label>*/}
                <div className="dropdown">
                    <button className="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown"
                            id={station + "-station-dropdown-button"}>
                        {this.props.chosenIata}
                    </button>
                    <ul id={station + "-station-dropdown"} className="dropdown-menu pointer">
                        {dropdownMenuElements}
                    </ul>
                </div>
            </div>
        )
    }
}

function DropdownMenuElement(props) {
    return (<li className="dropdown-item" onClick={props.onClick}>{props.iata}</li>);
}

class DatePicker extends React.Component {
    constructor(props) {
        super(props);
        this.handleFlightDateChange = this.handleFlightDateChange.bind(this);
    }

    handleFlightDateChange(event) {
        this.props.onFlightDateChange(event.target.value)
    }

    render() {
        return (
            <div>
                <label htmlFor="flight-date">Flight date</label>
                <div>
                    <input id="flight-date" type="date" defaultValue={this.props.flightDate}
                           onChange={this.handleFlightDateChange}/>
                </div>
            </div>
        )
    }
}

class UpdateButton extends React.Component {
    constructor(props) {
        super(props);
        this.handleClick = this.handleClick.bind(this);
    }

    handleClick() {
        this.props.onClick()
    }

    render() {
        return (
            <button className="btn btn-primary" type="button" onClick={this.handleClick}>Update</button>
        )
    }
}

class Diagram extends React.Component {
    constructor(props){
        super(props)
    }

    componentDidUpdate(){
        tippy('[title]');
    }

    getMaxPriceFromFlights(flights) {
        var maxPrice = 0;
        for (var key in flights) {
            if (flights[key].priceInHuf > maxPrice) {
                maxPrice = flights[key].priceInHuf;
            }
        }
        return maxPrice;
    }

    getDateMapFromSearchDate(searchDate) {
        let splittedSearchDate = searchDate.split("-");
        let fullYear = splittedSearchDate[0];
        let dateMap = {
            "year": fullYear.substring(2, fullYear.length),
            "month": splittedSearchDate[1],
            "day": splittedSearchDate[2]
        }
        return dateMap;
    }

    render(){
        // egy oszlopban különböző színnel a különböző időpontban lekért árak
        let diagramElements = [];
        let diagramElementDates = [];
        let flights = this.props.flights;
        var maxPrice = this.getMaxPriceFromFlights(flights);
        for (var searchDate in flights) {
            let flight = flights[searchDate];
            let pricePercentage = (flight.priceInHuf / maxPrice) * 100;
            diagramElements.push(<DiagramElement key={flight.id}
                                                 searchDateTime={searchDate}
                                                 priceInHuf={flight.priceInHuf}
                                                 pricePercentage={pricePercentage}/>);

            let dateMap = this.getDateMapFromSearchDate(searchDate);
            diagramElementDates.push(<DiagramElementDate dateMap={dateMap} key={searchDate}/>)
        }

        return(
            <div className="diagram-wrapper">
                <div className="diagram-title">From {this.props.departureStation} to {this.props.arrivalStation}</div>
                <div className="table-row">
                    <div className="diagram-y-axis-details table-cell">
                        {maxPrice}
                    </div>
                    <div className="diagram-body table-cell">
                        {diagramElements}
                    </div>
                </div>
                <div className="table-row">
                    <div className="origo table-cell"/>
                    <div className="diagram-x-axis-details table-cell">
                        {diagramElementDates}
                    </div>
                </div>
            </div>
        )
    }
}

function DiagramElement(props) {
    return (
        <div className="diagram-element table-cell">
            <div className="diagram-element-column bg-success" style={{height: props.pricePercentage*2+'px'}} title={props.priceInHuf}/>
        </div>
    )
}

function DiagramElementDate(props) {
    return (
        <div className="diagram-element-date">
            <div>{props.dateMap.day}</div>
            <div>-</div>
            <div>{props.dateMap.month}</div>
            <div>-</div>
            <div>{props.dateMap.year}</div>
        </div>
    )
}

ReactDOM.render(
    <PriceDiagram/>
    , document.getElementById('price-diagram')
);