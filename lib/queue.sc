// little helper class
// to make it obvious what's going on
// a queue is a list you add to the front of
// and take from the back of
// so first in, first out
Queue : List {

	enqueue {
		| n |
		this.addFirst(n);
	}

	dequeue {
		^this.pop;
	}

}