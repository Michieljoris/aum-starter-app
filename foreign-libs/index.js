//Add npm dep to package.json, then collect what you need here by importing it
//into this index.js module, and adding globals. Then add the globals to
//foreign-libs in revolt.edn as namespaces

import React from 'react';
import ReactDOM from 'react-dom';
window.React = React;
window.ReactDOM = ReactDOM;

import Datetime from 'react-datetime';
window.ReactDatetime = Datetime;

import moment from "moment";
window.moment = moment;

import * as SemanticUIReact from 'semantic-ui-react';
window.SemanticUIReact = SemanticUIReact;
