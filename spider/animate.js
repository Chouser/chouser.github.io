document.addEventListener('click', () => {
  const svg = document.getElementsByTagName('svg')[0]
  svg.requestFullscreen()
  svg.setAttribute('width',  window.innerWidth  + 'px')
  svg.setAttribute('height', window.innerHeight + 'px')
})

const setAngle = (id, angle) => {
  const g = document.getElementById(id)
  const r = g && g.getAttribute('transform')
  r || console.log("missing: '" + id + "'");
  g.setAttribute('transform', r.replace(/(?<=\().*?(?=,)/, angle))
}

const applyState = (state) => {
  for(var i = 0; i < 4; ++i) {
    [i*2, i*2+1].forEach( n => ['a', 'b', 'c'].forEach( s =>
      setAngle('leg' + (n + 1) + s,
        Math.sin(state.legOffset + i * Math.PI * 3 / 2) * 10 - 5)
    ))
  }

  [1,2].forEach( n => {
    setAngle('m' + n + 'a', Math.sin(state.mandibleOffset)*10 - 5)
    setAngle('m' + n + 'b', Math.sin(state.mandibleOffset)*5 - 5)
  })

  document.getElementById('spider-rotate').setAttribute(
    'transform',
    `translate(${state.spiderX},${state.spiderY}) rotate(${state.spiderAngle},150,170)`
  )
}

const crawlForward = (t, state, distance, speed) => {
  const a = state.spiderAngle / 180 * Math.PI
  const toX = state.spiderX + Math.cos(a) * distance;
  const toY = state.spiderY + Math.sin(a) * distance;

  return {
    start_t: t,
    duration: 40 * distance / speed,
    from_state: {...state},
    to_state: {...state, spiderX: toX, spiderY: toY},
    leg_speed: speed
  }
}

const turn = (t, state, angle_delta, speed) => {
  return {
    start_t: t,
    duration: 100 * Math.abs(angle_delta) / speed,
    from_state: {...state},
    to_state: {
      ...state,
      spiderAngle: state.spiderAngle + angle_delta
    },
    leg_speed: speed
  }
}

const wait1 = (t) => {
  return {
    start_t: t,
    duration: 1000,
    from_state: {...state},
    to_state: {...state},
    leg_speed: 0
  }
}

const init_state = {
  legOffset: 0,
  mandibleOffset: 0,
  spiderAngle: 0,
  spiderX: -300,
  spiderY: 50
};

var state;
var interp = {};

const reset = (t) => {
  state = {...init_state};
  interp = crawlForward(t, state, 500, 5);
}

const aimCenter = (t) => {
  const dy = 50 - state.spiderY;
  const dx = 200 - state.spiderX;
  const a = Math.atan2(dy, dx) / Math.PI * 180;
  const dist = Math.sqrt(Math.abs(dy * dy) + Math.abs(dx * dx))
  state = {...state, spiderAngle: a}
  interp = crawlForward(t, state, dist + Math.random() * 200 - 100, Math.random() * 5 + 2);
}

const tick = t => {
  if(!interp.start_t) reset(t);

  state.legOffset = (t - interp.start_t) / 1000 * interp.leg_speed + interp.from_state.legOffset;
  const m = Math.min(1, (t - interp.start_t) / interp.duration);
  ['spiderX', 'spiderY', 'spiderAngle'].forEach( f =>
    state[f] = (interp.to_state[f] - interp.from_state[f]) * m + interp.from_state[f]
  )

  //state.mandibleOffset = Math.min(500, Math.abs(500 - (t % 6000))) / 500 * 90

  applyState(state);

  if(m == 1) {
    // pick new interp
    if(state.spiderX < -300 || 650 < state.spiderX || state.spiderY < -350 || 450 < state.spiderY) {
      // out of bounds
      aimCenter(t);
    }
    else {
      const act = Math.random();
      if( act < 0.8 )
        interp = wait1(t)
      else if( act < 0.9)
        interp = turn(t, state, Math.random() * 360 - 180, Math.random() * 5 + 2);
      else
        interp = crawlForward(t, state, Math.random() * 500 + 100, Math.random() * 5 + 2);
    }
  }

  window.requestAnimationFrame(tick)
}

window.requestAnimationFrame(tick)
