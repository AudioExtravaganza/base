/*********************************************************************
Extension: DAM GUI
Authors: Mason Sidebottom
Purpose: Provides a basic interface for testing DAM Good Pedal
		 functionality virtually
Date: Feb. 2019
*********************************************************************/

/*********************************************************************
DAMComponent
	Serves as the base class for other components. Gives an interface
	for binding actions and setting up GUI components
**********************************************************************/
DAMComponent{
	// Displays the name of the component
	var <>label;

	// Dictionary of actions (simple interface to add/remove)
	var <>actions;

	// The componenet itself. Expects a GUI component that recieves input
	// 	EX. Knob, Button, Menu
	var <>component;

	var <>dispatcher;
	var <>id;
	var <>type;
	/******************************************************************
	Build
		Basic constructor. Must be explicitly called after an instance
		is created
		Parameters: parent view, position in parent, and Name
		! If using with DAMDebugger, all names must be unique

		! In classes that extend this, set the component before calling
		! 	super.
		!	ex.
		!		this.component = Knob(...);
		!		super(parent, pos, name);
	*******************************************************************/
	build {

		arg parent = 0, pos = Rect(0,0, 100, 100) , name = "Blank Component", id = -1, type = '-';

		// Label Position
		var lPos = pos.asArray();
		// Offset 30 pixels from the bottom of the component
		lPos = Rect(lPos[0] - 15, lPos[1] + 50, lPos[2] + 30, 20);

		// Generate Static text for label, at LPos
		this.label = StaticText(parent, lPos);
		this.label.string = name;

		// Center Text
		this.label.align = \center;

		// Set font
		this.label.font_(Font("Lucida Sans", 12));

		// Use dictionary for actions
		this.actions = Dictionary(0);

		// Bind action of component
		// 	Call runActions so classes that extend this
		//	can overwrite runActions
		this.component.action_({
			arg comp;
			this.runActions(comp);
		});

		this.id = id;
		this.type = type;
	}

	/******************************************************************
	Run Actions
		Iterates through action dictionary calling functions.

		!Do not explicitly call, the component will call this on update
		! Instead use:
		!		this.component.valueAction = this.component.value;
		! or
		!		this.forceAction();
	*******************************************************************/
	runActions {
		arg comp;

		// For all actions
		this.actions.asArray.do{
			arg func, i;
			// Call actions, send name and value
			func.value(this.getName(), comp.value);
		}
	}

	/******************************************************************
	Bind OSC
		Override bind OSC from base class to send the string from menu
	*******************************************************************/
	bindOSC {
		// Create the dispatcher
		this.dispatcher = NetAddr.new("127.0.0.1", 57120);

		// Add dispatch action
		this.addAction("dispatch", {
			arg name, val;

			// Send the message name, with the parameter component value
			this.dispatcher.sendMsg("/" ++ this.type, this.id, this.component.value);
		});

	}

	/******************************************************************
	Add Action
		Adds an action
		Parameters: name of action and the action itself (function)
	*******************************************************************/
	addAction{
		arg name, func;
		this.actions.add(name -> func);
	}

	/******************************************************************
	Remove Action
		Removes an action
		Parameters: name of action

		! Assumes that an action with the name provided exists.
	*******************************************************************/
	removeAction{
		arg name;
		this.actions.removeAt(name);
	}

	/******************************************************************
	Force Action
		Safely forces actions to be called
	*******************************************************************/
	forceAction{
		this.component.valueAction = this.component.value;
	}

	/******************************************************************
	Get Name
		Gets name of component from label.

		Returns: Name of component
		! If using with DAMDebugger, names must be unique
	*******************************************************************/
	getName{
		^this.label.string;
	}

	/******************************************************************
	Free
		Frees the componenet and dispatcher

		! If you allocate anything other than the componenet
		! or dispatcher in a child class, create a new free method and
		! use 'super.free;'
	*******************************************************************/
	free {
		this.component.free;
		this.dispatcher.free;
	}

	updateGUI {
		arg newVal;
		this.component.value = newVal;
		this.component.valueAction = newVal;
	}
}


/*********************************************************************
DAMKnob
	Extends the DAMComponent to implement basic knobs
**********************************************************************/
DAMKnob : DAMComponent {

	/******************************************************************
	Build
		Basic constructor. Must be explicitly called after an instance
		is created
		Parameters: parent view, position in parent, and name
		! If using with DAMDebugger, all names must be unique
	*******************************************************************/
	build {
		arg parent = 0, rect = Rect(0,0, 50, 50), name = "Knob", id = -1;

		// Set the component
		this.component = Knob.new(parent, rect);
		this.component.valueAction = 0.5;

		// Let the base class build itself
		super.build(parent, rect, name, id, 'knob');
	}

	inc{
		arg pos = true, fast = false, slow = false;
		var speed = -0.05;
		if(fast){speed = -0.1};
		if(slow){speed = -0.01};
		if(pos){speed = speed * -1};
		this.component.valueAction = this.component.value + speed;
	}

}

/*********************************************************************
DAMMenu
	Extends the DAMComponent to implement a dropdown menu.
**********************************************************************/
DAMMenu : DAMComponent {
	// Menu items
	var <> items;

	/******************************************************************
	Build
		Basic constructor. Must be explicitly called after an instance
		is created
		Parameters: parent view, position in parent, and name
		! If using with DAMDebugger, all names must be unique
	*******************************************************************/
	build {
		arg parent = 0, rect = Rect(0,0, 50, 50), name = "Menu", id = -1;

		// Create menu
		this.component = PopUpMenu(parent, rect);

		// Set items to empty list
		this.items = List(0);

		// Set menu items to item list, cast to array
		this.component.items = this.items.asArray();

		// Let the base class build itself
		super.build(parent, rect, name, id, 'm');

		// Make label invisible
		this.label.visible = false;
	}

	left {
		var v = this.component.value;

		if(v > 0){
			this.component.valueAction = v - 1;
		};
	}
	right {
		var v = this.component.value;
		if(v < (this.items.size - 1)){
			this.component.valueAction = v + 1;
		};
	}
	/******************************************************************
	Run Actions
		Override run actions from base class

		!Do not explicitly call, the component will call this on update
		! Instead use:
		!		this.component.valueAction = this.component.value;
		! or
		!		this.forceAction();
	*******************************************************************/
	runActions {
		arg m;
		// If no menu items exist, action is meaningless
		if(this.items.size >= 1){

			// For all actions
			this.actions.asArray.do{
				arg func, i;

				// Send the action function parameters in form
				//	(name, [index, menu_item])
				func.value(this.getName(), [m.value, this.items.at(m.value)]);
			};
		};
	}

	updateGUI {
		arg val;
		this.items.do{
			arg item, i;
			if(val == item){
				this.component.valueAction = i;
			};
		};
	}

	/******************************************************************
	Bind OSC
		Override bind OSC from base class to send the string from menu
	*******************************************************************/
	bindOSC {
		// Create the dispatcher
		this.dispatcher = NetAddr.new("127.0.0.1", 57120);

		// Add dispatch action
		this.addAction("dispatch", {
			arg name, val;

			// Send the message name, with the parameter menu string
			this.dispatcher.sendMsg("/menu", this.items.at(this.component.value));
		});

	}

	/******************************************************************
	Add Item
		Adds an Item to the menu
		Parameters: String of item to add to menu.
	*******************************************************************/
	addItem {
		arg item;
		this.items.add(item);

		// Update menu items to item list, cast to array
		this.component.items = this.items.asArray();
		if(this.items.size == 1){
			this.component.valueAction = this.items[0];
		}
	}

	/******************************************************************
	Remove Item
		Adds an Item to the menu
		Parameters: String of item to be removed
	*******************************************************************/
	removeItem {
		arg item;

		// Linear search for item
		this.items.do{
			arg it, i;
			// If found, remove it
			if(it == item){
				this.items.removeAt(i);
			};
		};

		// Update menu items to item list, cast to array
		this.component.items = this.items.asArray();
	}

	/******************************************************************
	Current
		Gets current menu value
	*******************************************************************/
	current {
		^this.items.at(this.component.value);
	}

}

/*********************************************************************
DAMMenu
	Extends the DAMComponent to implement a pedal using buttons
**********************************************************************/
DAMPedal : DAMComponent {

	/******************************************************************
	Build
		Basic constructor. Must be explicitly called after an instance
		is created
		Parameters: parent view, position in parent, and name
		! If using with DAMDebugger, all names must be unique
	*******************************************************************/
	build {
		arg parent = 0, rect = Rect(0,0, 50, 50), name = "Pedal", id = -1;

		// Build button
		this.component = Button(parent, rect);

		// Set the states to:
		//	0: Off
		// 	1: On
		this.component.states = [["Off", Color.white, Color.red], ["On", Color.black, Color.green], ["Hold", Color.cyan]];

		// Default to off
		this.component.value = 0;

		// Let the base class build itself
		super.build(parent, rect, name, id, 'pedal');
	}

	toggle {
		arg holding = false, off = false;

		if(holding){
			this.component.valueAction = 2;
			^2;
		};
		if(off) {
			this.component.valueAction = 0;
			^0;
		};

		if(this.component.value > 0){
			this.component.valueAction = 0;
			^0;
		};

		this.component.valueAction = 1;
		^1;
	}

	/******************************************************************
	Run Actions
		Override run actions from base class

		!Do not explicitly call, the component will call this on update
		! Instead use:
		!		this.component.valueAction = this.component.value;
		! or
		!		this.forceAction();
	*******************************************************************/
	runActions {
		arg b;
		this.actions.asArray.do{
			arg func, i;

			// Send the action function parameters in form
			//	(name, [index, button state])
			func.value(this.getName, [b.value, this.component.states.at(b.value).at(0)]);
		}
	}
}


/*********************************************************************
DAMDebugger
	Displays information about DAMComponents.
**********************************************************************/
DAMDebugger {

	// View (allows relative positioning)
	var <> view;

	// Current x and y values
	var <>x, <>y;

	// All fields in Debugger
	var <>fields;

	/******************************************************************
	Build
		Basic constructor. Must be explicitly called after an instance
		is created
		Parameters: parent view, position in parent
	*******************************************************************/
	build {
		arg parent = 0, rect = Rect(0, 0, 50, 50);

		// Create new view
		this.view = CompositeView.new(parent, rect);

		// Set fields to empty dictionary
		this.fields = Dictionary(0);

		// Set start postion
		this.x = 5;
		this.y = 10;
	}


	/******************************************************************
	Add Field
		Adds a text field for a new DAMComponent.
		Parameters: Name of DAMComponent
	*******************************************************************/
	addField {
		arg name;

		var label, value;

		// Build 2 StaticTexts one for label and the other to show value
		label = StaticText(this.view, Rect(this.x, this.y, 140, 20));
		value = StaticText(this.view, Rect(this.x + 140, this.y, 210, 20));

		// Set fonts
		label.font_(Font("Lucida Sans", 12));
		value.font_(Font("Lucida Sans", 12));

		// Setup label and default value to "Not Set"
		label.string = name ++ " value:";
		value.string = "Not Set";

		// Add the two text fields to the dictionary
		this.fields.add(name ->[label, value]);

		// Increment y by 20
		this.y = this.y + 20;

		// If out of bounds, reset y and move to the right 250.
		if(this.y > (this.view.bounds.asArray()[3])){
			this.y = 10;
			this.x = this.x + 400;
		};
	}

	/******************************************************************
	Bind Float
		Binds the debugger to a DAMComponent that outputs floats
		Parameters: DAMComponenet

		! Make sure all DAMComponents that are bound to the DAMDebugger
		! have unique names
	*******************************************************************/
	bindFloat {
		arg item;

		// Create a field for the component
		this.addField(item.getName());

		// Add an action to the DAMComponent
		item.addAction("debugAction", {
			arg name, value;

			// Using the name of the component, modify the value of
			// The StaticText
			this.fields.at(name)[1].string = value.asStringPrec(3)
		});

		// Force Action on item to update debugger
		item.forceAction();
	}

	/******************************************************************
	Bind String
		Binds the debugger to a DAMComponent that outputs as a string
		Parameters: DAMComponenet

		! Make sure all DAMComponents that are bound to the DAMDebugger
		! have unique names
	*******************************************************************/
	bindString {
		arg item;

		// Create a field for the component
		this.addField(item.getName());

		// Add an action to the DAMComponent
		item.addAction("debugAction", {
			arg name, value;

			// Using the name of the component, modify the value of
			// The StaticText
			this.fields.at(name)[1].string = value
		});

		// Force Action on item to update debugger
		item.forceAction();
	}

	/******************************************************************
	Free
		Frees all fields
	*******************************************************************/
	free {

		// For all fields
		this.fields.do{
			arg f;

			// Free
			f.free;
		}
	}
}

/*********************************************************************
DAM GUI
	Class that implements the virtual front end of the DAM GOOD Pedal
**********************************************************************/
DAMGUI {

	// Window
	var <>win;

	// DAMComponents
	var <>knobs;
	var <>menu;
	var <>pedals;

	// Other output
	var <>dbg;
	var <>scope;
	var <> freeActions;

	/******************************************************************
	Build
		Basic constructor. Must be explicitly called after an instance
		is created
		Parameters: Debugging (true/false)
	*******************************************************************/
	build {
		arg debug = false, useOSC = true;

		var x, y, h;


		// Default height to 400
		h = 600;

		// If debugging active, set to 600
		if(debug){
			h = 800;
		};

		// Create window
		this.win = Window.new("DAM Good Pedal", Rect(200, 200, 800, h)).front;

		// Setup arrays of X, Y values for knobs
		x = [135, 295, 455, 615];
		y = Array.fill(4, {arg i; 300});

		// Create 4 DAMKnobs
		this.knobs = Array.fill(4, {arg i; DAMKnob.new();});


		// Build the Knobs with the correct names and positions
		this.knobs.do{
			arg item, i;
			item.build(this.win, Rect(x[i], y[i], 50, 50), ("Knob " ++ (i+1).asDigit), i);
			if (useOSC){
				item.bindOSC();
			};
		};


		// Set X values for the DAMPedals
		x = [175, 375, 575];

		// Create 3 DAMPedals
		this.pedals = Array.fill(3, {arg i; DAMPedal.new();});

		// Build the Pedals with the correct names and positions
		this.pedals.do{
			arg item, i;
			item.build(this.win, Rect(x[i], 450, 50, 50), ("Pedal " ++ (i + 1).asDigit), i);
			if (useOSC){
				item.bindOSC();
			};
		};


		this.win.view.keyDownAction = {
			arg view, char, mod, uni, keycode, key;
			var hold, off, holdM, offM;
			hold = (mod == 131072);
			off = (mod == 262144);
			holdM = (mod == 1048576);
			offM = (mod == 131072);
			// keycode.postln;
			// mod.postln;
			case
				{keycode == 49  } {this.pedals[0].toggle(hold, off);}
				{keycode == 50  } {this.pedals[1].toggle(hold, off);}
				{keycode == 51  } {this.pedals[2].toggle(hold, off);}
				{keycode == 65  } {this.knobs.[0].inc(false, hold, off);}
				{keycode == 83  } {this.knobs.[0].inc(true, hold, off);}
				{keycode == 68  } {this.knobs.[1].inc(false, hold, off);}
				{keycode == 70  } {this.knobs.[1].inc(true, hold, off);}
				{keycode == 71  } {this.knobs.[2].inc(false, hold, off);}
				{keycode == 72  } {this.knobs.[2].inc(true, hold, off);}
				{keycode == 74  } {this.knobs.[3].inc(false, hold, off);}
				{keycode == 75  } {this.knobs.[3].inc(true, hold, off);}
				{keycode == 188 } {this.menu.left()}
				{keycode == 190 } {this.menu.right()}
				{keycode == 18} {this.pedals[0].toggle(holdM, off);}
				{keycode == 19} {this.pedals[1].toggle(holdM, off);}
				{keycode == 20} {this.pedals[2].toggle(holdM, off);}
				{keycode == 0 } {this.knobs.[0].inc(false, holdM, offM);}
				{keycode == 1 } {this.knobs.[0].inc(true, holdM, offM);}
				{keycode == 2 } {this.knobs.[1].inc(false, holdM, offM);}
				{keycode == 3 } {this.knobs.[1].inc(true, holdM, offM);}
				{keycode == 5 } {this.knobs.[2].inc(false, holdM, offM);}
				{keycode == 4 } {this.knobs.[2].inc(true, holdM, offM);}
				{keycode == 38} {this.knobs.[3].inc(false, holdM, offM);}
				{keycode == 40} {this.knobs.[3].inc(true, holdM, offM);}
				{keycode == 43} {this.menu.left()}
				{keycode == 47} {this.menu.right()}
			;
		};



		// Create and build the menu
		this.menu = DAMMenu.new();
		this.menu.build(this.win, Rect(160, 50, 320, 40), "Menu");
		if(useOSC){
			this.menu.bindOSC();
		};
		// Create the Frequency Scope
		this.scope = FreqScopeView(this.win, Rect(160, 90, 320, 150));
		this.scope.active = true;

		// Draw Rectangle around pedal interface
		this.win.drawFunc = {
			Color.black.set;
			Pen.moveTo(10@10);
			Pen.lineTo(10@590);
			Pen.lineTo(790@590);
			Pen.lineTo(790@10);
			Pen.lineTo(10@10);
			Pen.stroke;

			// If Debugging, draw rectangle around debugging
			//	interface
			if(debug){
				Pen.moveTo(10@610);
				Pen.lineTo(10@790);
				Pen.lineTo(790@790);
				Pen.lineTo(790@610);
				Pen.lineTo(10@610);
				Pen.stroke;
			}
		};
		this.win.refresh;

		// Defualt DBG to null
		this.dbg = nil;

		// If debugging, initialize debugger
		if(debug){
			this.initDebug();
		};

		// Bind event for window close
		this.win.onClose_({ this.free;});

		// Create new list of actions to go through on close
		this.freeActions = List(0);

		// Update GUI from OSC
		if(useOSC == false){
			thisProcess.addOSCRecvFunc({
				arg msg, time, addr;
				// Switch on the type
				switch(msg[0],
					// If knob, update knob bus
					'/knob', {this.knobs[msg[1]].updateGUI(msg[2]);},

					// If pedal, update pedal state
					'/pedal', {this.pedals[msg[1]].updateGUI(msg[2]);},

					// If menu, update menu string
					'/menu', {this.menu.updateGUI(msg[1]);}
				);
			});
		}
	}

	/******************************************************************
	Init Debug
		Sets up the debugger

		! Do not call outside of this class. If an instance exists with
		! debug set to false, it cannot add the debugger to the window
		! because there will not be enough space.
	*******************************************************************/
	initDebug {

		// Create and build the debugger
		this.dbg = DAMDebugger.new();
		this.dbg.build(this.win, Rect(10, 610, 780, 790));

		// Bind the knobs
		this.knobs.do{
			arg item, i;
			this.dbg.bindFloat(item);
		};

		// Bind the menu
		this.dbg.bindString(menu);

		// Bind the pedals
		this.pedals.do{
			arg item, i;
			this.dbg.bindString(item);
		};
	}


	/******************************************************************
	Add Scene
		Adds name to the menu
	*******************************************************************/
	addScene {
		arg name;
		this.menu.addItem(name);
	}


	/******************************************************************
	Force OSC Update
		Makes all OSC objects run through actions
	*******************************************************************/
	forceOSCUpdate {

		// For all knobs
		this.knobs.do{
			arg k;

			// Force action
			k.forceAction;
		};

		// Force menu cation
		this.menu.forceAction;

		// For all pedals
		this.pedals.do{
			arg p;

			// Force action
			p.forceAction;
		};
	}


	/******************************************************************
	Add Close Action
		Adds an action to do when the window is closed

		Parameters: action, a function to be executed on window close
	*******************************************************************/
	addCloseAction{
		arg action;

		// Adds the action to the array of free actions
		this.freeActions.add(action);
	}

	/******************************************************************
	Free
		Frees all the componenets belonging to this class
	*******************************************************************/
	free{

		// Kill the scope
		this.scope.kill;

		// For all Knobs
		this.knobs.do{
			arg k;

			// Free
			k.free;
		};

		// Free the menu
		this.menu.free;

		// For all pedals
		this.pedals.do{
			arg p;

			// Free
			p.free;
		};

		// If the debugger is not null, free it
		if(this.dbg != nil){
			this.dbg.free;
		};

		// For all free actions
		this.freeActions.do{
			arg act;

			// Execute
			act.value;
		}
	}
}
