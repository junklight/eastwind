-- Eastwind: additive gestures 
-- 1.0.0 @junklight
-- l.llllllll.co/eastwind
--
-- 
--


engine.name ='Eastwind'
eastwind_params = include 'lib/eastwind_params'
gc = include 'lib/gridcontrols'
lc = include 'lib/loopercontrols'
g = grid.connect()

controls = {}
table.insert(controls,lc.new(1,1))
table.insert(controls,lc.new(2,2))
table.insert(controls,gc.new(3, function () return params:get("attack") end, function (x) return params:set("attack",x) end ,{0.1,0.3,0.5,1,5,10}))
table.insert(controls,gc.new(4, function () return params:get("release") end, function (x) return params:set("release",x) end  ,{0.1,0.3,0.5,1,5,10}))
table.insert(controls,gc.new(5, function () return params:get("smix") end, function (x) return params:set("smix",x) end  ,{0,0.25,0.4,0.6,0.75,1}))
table.insert(controls,gc.new(6, function () return params:get("decay") end, function (x) return params:set("decay",x) end  ,{0,1,2,4,10,20}))
table.insert(controls,gc.new(7, function () return params:get("fltfreq") end, function (x) return params:set("fltfreq",x) end  ,{0.1,0.6,0.9,1.2,1.4,2}))
table.insert(controls,gc.new(8, function () return params:get("fltres") end, function (x) return params:set("fltres",x) end  ,{0.1,0.5,1,2,3,4}))


function init()
  eastwind_params.add_params()
  grid_dirty = false 

  momentary = {} 
  for x = 1,10 do 
    momentary[x] = {} 
    for y = 1,8 do 
      momentary[x][y] = false 
    end
  end
  
  clock.run(grid_redraw_clock) 
  
  
end


function key(n,z)
  
  
  
end

function redraw()
  screen.clear()
  screen.move(64,32)
  screen.text("eastwind")
  screen.update()
end

function grid_redraw_clock() 
  cnt = 0
  while true do 
    cnt = cnt + 1
    if cnt % 100 == 0 then
      engine.dnodes()
    end
    clock.sleep(1/30) 
    if true or grid_dirty then 
      grid_redraw() 
      grid_dirty = false 
    end
  end
end

function grid_redraw() 
  g:all(0) 
  g:led(11,1,15)
  for x = 1,10 do 
    for y = 1,8 do 
      if momentary[x][y] then 
        g:led(x,y,15) 
      end
    end
  end
  for k,v in ipairs(controls) do
    v:draw(g)
  end
  g:refresh() 
end

function g.key(x,y,z)  
  if x <= 10 then 
    key = (((y - 1) * 10) + x ) - 1
    if z == 1 then
      engine.noteon(key)
    else
      engine.noteoff(key)
    end
    momentary[x][y] = z == 1 and true or false 
  else
    for k,v in ipairs(controls) do
      v:key(x,y,z)  
    end 
  end
  
  
  grid_dirty = true 
end
