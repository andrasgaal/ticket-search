class PriceDiagram extends React.Component {
    constructor() {
        super();
        this.state = {
            iatas: []
        }
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

    render(){
        return (
            <div>
                <Dropdown iatas={this.state.iatas} station={"departure"} defaultIata={"BUD"} labelText={"From"}/>
                <div className="separator"/>
                <Dropdown iatas={this.state.iatas} station={"arrival"} defaultIata={"LTN"} labelText={"To"}/>
                <div className="separator"/>
                <DatePicker/>
                <div className="separator"/>
                <UpdateButton/>
            </div>
        );
    }
}

class Dropdown extends React.Component {
    constructor(props) {
        super(props);
        this.state = {chosenIata : this.props.defaultIata}
    }

    handleClick(iata) {
        this.setState({chosenIata: iata})
    }

    render(){
        let station = this.props.station;

        let self = this;
        let dropdownMenuElements = [];
        this.props.iatas.forEach(function (iata) {
            dropdownMenuElements.push(<DropdownMenuElement key={iata} iata={iata} onClick={() => self.handleClick(iata)}/>);
        });

        return (
            <div>
                <label htmlFor={station +"-station-dropdown-button"}>{this.props.labelText}</label>
                <div className="dropdown">
                    <button className="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown" id={station +"-station-dropdown-button"}>
                        {this.state.chosenIata}
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

    componentDidMount(){
        document.getElementById("flight-date").valueAsDate = new Date()
    }

    render() {
        return (
            <div>
                <label htmlFor="flight-date">Flight date</label>
                <div>
                    <input id="flight-date" type="date"/>
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
        let departureStation = $("#departure-station-dropdown-button").text();
        let arrivalStation = $("#arrival-station-dropdown-button").text();
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