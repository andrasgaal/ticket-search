class PriceDiagram extends React.Component {
    constructor() {
        super();
        let todayIso = new Date().toISOString();
        let today = todayIso.substring(0, todayIso.indexOf("T"));
        this.state = {
            iatas: [],
            departureStation: "BUD",
            arrivalStation: "LTN",
            flightDate: today
        };
        this.handleDepartureStationChange = this.handleDepartureStationChange.bind(this);
        this.handleArrivalStationChange = this.handleArrivalStationChange.bind(this);
        this.handleFlightDateChange = this.handleFlightDateChange.bind(this);
    }

    componentDidMount(){
        this.getIatas();
    }

    getIatas(){
        let self = this;
        $.ajax({
            url: "http://localhost:8080/iatas"
        }).done(function (data) {
            self.setState({iatas: data});
        });
    }

    handleDepartureStationChange(iata) {
        this.setState({"departureStation": iata});
    }

    handleArrivalStationChange(iata) {
        this.setState({"arrivalStation": iata});
    }

    handleFlightDateChange(date){
        this.setState({"flightDate": date});
    }

    render(){
        return (
            <div>
                <Dropdown iatas={this.state.iatas} station={"departure"} chosenIata={this.state.departureStation} labelText={"From"} onStationChange={this.handleDepartureStationChange}/>
                <div className="separator"/>
                <Dropdown iatas={this.state.iatas} station={"arrival"} chosenIata={this.state.arrivalStation} labelText={"To"} onStationChange={this.handleArrivalStationChange}/>
                <div className="separator"/>
                <DatePicker flightDate={this.state.flightDate} onFlightDateChange={this.handleFlightDateChange}/>
                <div className="separator"/>
                <UpdateButton/>
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

    render(){
        let station = this.props.station;

        let self = this;
        let dropdownMenuElements = [];
        this.props.iatas.forEach(function (iata) {
            dropdownMenuElements.push(<DropdownMenuElement key={iata} iata={iata} onClick={() => self.handleIataChange(iata)}/>);
        });

        return (
            <div>
                <label htmlFor={station +"-station-dropdown-button"}>{this.props.labelText}</label>
                <div className="dropdown">
                    <button className="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown" id={station +"-station-dropdown-button"}>
                        {this.props.chosenIata}
                    </button>
                    <ul id={station +"-station-dropdown"} className="dropdown-menu pointer">
                        {dropdownMenuElements}
                    </ul>
                </div>
            </div>
        )
    }
}

function DropdownMenuElement(props){
    return (<li className="dropdown-item" onClick={props.onClick}>{props.iata}</li>);
}

class DatePicker extends React.Component {
    constructor() {
        super();
    }

    handleFlightDateChange(){
        //TODO
    }

    render() {
        return (
            <div>
                <label htmlFor="flight-date">Flight date</label>
                <div>
                    <input id="flight-date" type="date" defaultValue={this.props.flightDate} onChange={this.handleFlightDateChange}/>
                </div>
            </div>
        )
    }
}

class UpdateButton extends React.Component{
    constructor(){
        super();
    }

    updateDiagram() {
        let flightDate = $("#flight-date").val();
        $.ajax({
            url: "http://localhost:8080/flights?departureStation="+departureStation+"&arrivalStation="+arrivalStation+"&flightDate="+flightDate
        }).done(function (data) {
            console.log(data);
            $("#diagram-data").text(data)
        });
    }

    render() {
        return (
            <div>
                <button className="btn btn-primary" type="button" onClick={() => this.updateDiagram()}>Update</button>
                <div className="separator"/>
                <div id="diagram-data"></div>
            </div>
        )
    }
}

ReactDOM.render(
    <PriceDiagram/>
    ,document.getElementById('price-diagram')
);