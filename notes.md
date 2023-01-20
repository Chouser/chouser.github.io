# mapcatseq

Code to supplement a (blog post)[resources/index.html]

## Additional notes
Laziness is usually, though not always, considered a good thing in this context.
"I get more done when I'm lazy"

(apply concat xs)
Laziness level: 4 (likes to get a general sense of how things are going to go, but in no hurry to do them)
Winner of the "favorite" poll. Maybe the easiest to understand. The word "con"
might make you think of a con-man who's trying to trick you into things, but
it's really quite harmless.

(mapcat identity xs)
Laziness level: 4 (likes to get a general sense of how things are going to go, but in no hurry to do them)
Clearly a close cousin to "mapcat seq", but much more popular in the poll,
despite its longer name. Only difference is in the slightly greater laziness. Is
actually made out of "apply concat"

(mapcat seq xs)
Laziness level: 3 (mostly lazy, but occasionally eager in brief bursts)
A close cousin of "mapcat identity", but proud of its shorter name. Also made
out of "apply concat". Lost the "favorite" poll, but used to be *my* favorite,
at least until I started looking at all these others.

(flatten xs)
Laziness level: 4 (likes to get a general sense of how things are going to go, but in no hurry to do them)
Second favorite in the poll, but I personally thinks it's a bit dangerous
because it does too much. Not that it's too eager, but like it doesn't know when
to stop and takes things too far, which can get you both into trouble. Also,
sometimes interprets instructions and just does things differently that the
others.

(sequence cat xs)
Laziness level: 2 (eager very briefly, then quite lazy)
The newest (youngest) of all.  Wasn't even included in the poll, but maybe people would really love it?
;; cat May 2012
;; sequence Feb 2009

(reduce into xs)
Laziness level: 1 (you could imagine something more eager, but not such thing exists)
Also pays more attention to containers than the others, in particular always
uses one of the containers you give it. As an unexpected consequence, somethings
gets things out of order, or can't get all the things to fit in the given
container and just gives up.

(lazier-mapcat identity xs)
Laziness level: 5 (the laziest. Won't do a darn thing until you absolutly insist, and then only as little as it can get away with.)
I made this one, so we can rename it if you want to.
Similar to "mapcat lazy" but is *not* related to "apply concat" at all. Since I
just made it up, nobody's ever heard of it.

(apply into xs)
Doesn't really belong, doesn't do what the others do, mostly doesn't work at
all. Doesn't need an illustration.
