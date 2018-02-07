var DropdownFill = React.createClass({

  getIatas: function () {
    var self = this;
    $.ajax({
      url: "http://localhost:8080/iatas"
    }).done(function(data) {
        self.setState({iatas: data});
      });
  },

  getInitialState: function () {
    return {iatas: []};
  },

  componentDidMount: function () {
    this.getIatas();
  },

  render() {
  var rows = [];
      this.props.iatas.forEach(function(iata) {
        rows.push(<DropdownMenuElement iata={iata}/>);
      });
      return ( <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">{rows}</div>);
    return ( <DropdownMenu iatas={this.state.iatas}/> );
  }
});

var DropdownMenu = React.createClass({
render: function() {
    var rows = [];
    this.props.iatas.forEach(function(iata) {
      rows.push(<DropdownMenuElement iata={iata}/>);
    });
    return ( <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">{rows}</div>);
  }
});

var DropdownMenuElement = React.createClass({
  render: function() {
    return (<a class="dropdown-item">{this.props.iata}</a>);
  }
});

ReactDOM.render(
  <DropdownFill/>, document.getElementById('dropdownMenu')
);