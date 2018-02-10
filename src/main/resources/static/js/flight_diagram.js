var DropdownFill = React.createClass({

    getIatas: function () {
        console.log("get iatas called");
        var self = this;
        $.ajax({
            url: "http://localhost:8080/iatas"
        }).done(function (data) {
            console.log("ajax call complete, got=" + data);
            self.setState({iatas: data});
        });
    },

    getInitialState: function () {
        console.log("component mounted");
        return {iatas: []};
    },

    componentDidMount: function () {
        console.log("get initial state");
        this.getIatas();
    },

    render: function () {
        console.log("dropdown state=" + this.state);
        let station = this.props.station;
        console.log("dropdown station  props=" + station);
        var rows = [];
        this.state.iatas.forEach(function (iata) {
            rows.push(<DropdownMenuElement iata={iata} station={station}/>);
        });
        return (<div>{rows}</div>);
    }
});

var DropdownMenuElement = React.createClass({
    handleClick() {
        let iata = this.props.iata;
        console.log(iata);
        console.log(this.props.station);
        $("#" + this.props.station + "-station").val(iata);
    },

    render: function () {
        console.log("rendering element = " + this.props.iata);
        return (<li className="dropdown-item" onClick={this.handleClick}>{this.props.iata}</li>);
    }
});

ReactDOM.render(
    <DropdownFill station="departure"/>, document.getElementById('departure-station-dropdown')
);

ReactDOM.render(
    <DropdownFill station="arrival"/>, document.getElementById('arrival-station-dropdown')
);