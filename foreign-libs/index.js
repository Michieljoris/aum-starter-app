//Add npm dep to package.json, then collect what you need here by importing it
//into this index.js module, and adding globals. Then add the globals to
//foreign-libs in revolt.edn as namespaces

import React from 'react';
import ReactDOM from 'react-dom';
window.React = React;
window.ReactDOM = ReactDOM;

import Datetime from 'react-datetime';
window.ReactDatetime = Datetime;

import moment from "moment"
window.moment = moment;


// import * as MaterialUI from 'material-ui';
// window.MaterialUI = MaterialUI;

// import * as MaterialUIStyles from 'material-ui/styles';
// window.MaterialUIStyles = MaterialUIStyles;

// import * as MaterialUISvgIcons from 'material-ui/svg-icons';
// window.MaterialUISvgIcons = MaterialUISvgIcons;

// import Bugsnag from "./bugsnag.js";
// window.Bugsnag = Bugsnag;

// import Dragula from "./dragula.js";
// window.Dragula = Dragula;

// import { Container, Draggable } from 'react-smooth-dnd';
// import ReactSmoothDnd from 'react-smooth-dnd';
// window.ReactSmoothDnd = ReactSmoothDnd;

// import { Container as SmoothDndContainer,
//          Draggable as SmoothDndDraggable} from 'react-smooth-dnd';
// window.ReactSmoothDnd = {Container: SmoothDndContainer,
//                          Draggable: SmoothDndDraggable}

// import Sortable from 'react-sortablejs';
// window.ReactSortable = Sortable;

// import { DragDropContext, Draggable, Droppable } from 'react-beautiful-dnd';
// window.ReactBeautifulDnd = {DragDropContext: DragDropContext,
//                             Draggable: Draggable,
//                             Droppable: Droppable}

import Tree, { mutateTree, moveItemOnTree } from '@atlaskit/tree';
window.AtlasKitTree = {Tree: Tree, mutateTree: mutateTree, moveItemOnTree: moveItemOnTree}

