class PriceDiagramSection extends React.Component {
    constructor() {
        super();
    }

    render() {
        return (
            <div>
                <div className="block">
                    <Diagram departureStation={this.props.departureStation} arrivalStation={this.props.arrivalStation} flights={this.props.outboundFlights}/>
                </div>
                <div className="block">
                    <Diagram departureStation={this.props.arrivalStation} arrivalStation={this.props.departureStation} flights={this.props.inboundFlights}/>
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
            <div className="date-picker">
                <label htmlFor="flight-date">Flight date</label>
                <div>
                    <input id="flight-date" type="date" defaultValue={this.props.flightDate}
                           onChange={this.handleFlightDateChange}/>
                </div>
            </div>
        )
    }
}

class Diagram extends React.Component {
    constructor(props){
        super(props)
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
        };
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


class HeatMapSection extends React.Component {
    constructor() {
        super();
        this.state = {
            flights: null,
            flightDateToSearchUntil: moment().add(3, "days").format("YYYY-MM-DD"),
            searchDatestoSearchFrom: moment().subtract(3, "days").format("YYYY-MM-DD")
        }
    }

    componentDidMount() {
        this.updateFlights();
    }

    updateFlights() {
        let self = this;
        $.ajax({
            url: "http://localhost:8080/flights/heatmap/"+this.props.departureStation+"/"+this.props.arrivalStation+
            "/"+this.state.flightDateToSearchUntil+"/"+this.state.searchDatestoSearchFrom
        }).done(function (flights) {
            self.setState({flights: flights})
        }).fail(function () {
            self.setState({flights: {}})
        });
    }

    render(){
        let flightsBySearchDate = this.state.flights;
        let heatmapElements = [];
        for(var searchDate in flightsBySearchDate) {
            let flightsByFlightDate = flightsBySearchDate[searchDate];
            for (var flightDate in flightsByFlightDate) {
                let flight = flightsByFlightDate[flightDate];
                heatmapElements.push(<HeatmapElement key={flight.id} flight={flight}/>)
            }
            heatmapElements.push(<div key={searchDate} className="table-row"/>)
        }

        if(this.state.flights){
            return (
                <div className="heatmap-diagram">
                    {heatmapElements}
                </div>
            )
        }
        return (<div>Loading heatmap...</div>)

    }
}

function HeatmapElement(props) {
    return (
        <div className="heatmap-element table-cell" title={props.flight.priceInHuf}/>
    )
}

class Dashboard extends React.Component {
    constructor() {
        super();
        this.state = {
            iatas: [],
            departureStation: "BUD",
            arrivalStation: "LTN",
            flightDate: moment().format("YYYY-MM-DD"),
            outboundFlights: {},
            inboundFlights: {}
        };

        this.handleDepartureStationChange = this.handleDepartureStationChange.bind(this);
        this.handleArrivalStationChange = this.handleArrivalStationChange.bind(this);
        this.handleFlightDateChange = this.handleFlightDateChange.bind(this)
        this.updateFlights = this.updateFlights.bind(this);
    }

    componentDidMount() {
        this.getIatas();
        this.updateFlights();
    }

    componentDidUpdate(){
        tippy('[title]');
    }

    getIatas() {
        let self = this;
        $.ajax({
            url: "http://localhost:8080/flights/iatas"
        }).done(function (data) {
            self.setState({iatas: data});
        }).fail(function () {
            self.setState({iatas: []});
        })
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

    updateFlights() {
        let self = this;
        $.ajax({
            url: "http://localhost:8080/flights/"+this.state.departureStation+"/"+
            this.state.arrivalStation+"/"+this.state.flightDate+"/groupby/searchdate"
        }).done(function (flights) {
            self.setState({outboundFlights: flights});
        }).fail(function () {
            self.setState({outboundFlights: {}});
        });

        $.ajax({
            url: "http://localhost:8080/flights/"+this.state.arrivalStation+"/"+
            this.state.departureStation+"/"+this.state.flightDate+"/groupby/searchdate"
        }).done(function (flights) {
            self.setState({"inboundFlights": flights});
        }).fail(function () {
            self.setState({"inboundFlights": {}});
        });
    }


    render(){
        return(
            <div>
                <div className="block">
                    <p className="title">Stations</p>
                    <Dropdown iatas={this.state.iatas} station={"departure"} chosenIata={this.state.departureStation}
                              onStationChange={this.handleDepartureStationChange}/>
                    <Dropdown iatas={this.state.iatas} station={"arrival"} chosenIata={this.state.arrivalStation}
                              onStationChange={this.handleArrivalStationChange}/>
                    <DatePicker flightDate={this.state.flightDate} onFlightDateChange={this.handleFlightDateChange}/>
                </div>
                <div className="section">
                    <PriceDiagramSection
                        iatas={this.state.iatas}
                        departureStation={this.state.departureStation}
                        arrivalStation={this.state.arrivalStation}
                        outboundFlights={this.state.outboundFlights}
                        inboundFlights={this.state.inboundFlights}
                    />
                </div>
                <div className="section">
                    <HeatMapSection
                        departureStation={this.state.departureStation}
                        arrivalStation={this.state.arrivalStation}
                    />
                </div>
            </div>
        )
    }
}

ReactDOM.render(
    <Dashboard/>,
    document.getElementById('dashboard')
);