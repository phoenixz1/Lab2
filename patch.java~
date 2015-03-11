
The issue we are referring to is the following: 

On the receipt of a missile tick (or every 200ms in the single player code base), the way the game moves the missiles is that it iterates through the projectileMap - which is a HashMap of the currently existing missiles - and calls the moveProjectile function on each projectile. What the original code base fails to check is if the missile to which the current iterator refers to has already been destroyed by another missile or not. The clean up of dead projectiles is done after iterating through all the projectiles and calling moveProjectile.

This is the code in MazeImpl.java where this happens:

                        if(!projectileMap.isEmpty()) {
                                Iterator it = projectileMap.keySet().iterator();
                                synchronized(projectileMap) {
                                        while(it.hasNext()) {   
                                                Object o = it.next();
                                                assert(o instanceof Projectile);
                                                deadPrj.addAll(moveProjectile((Projectile)o));
                                        }               
                                        it = deadPrj.iterator();
                                        while(it.hasNext()) {
                                                Object o = it.next();
                                                assert(o instanceof Projectile);
                                                Projectile prj = (Projectile)o;
                                                projectileMap.remove(prj);
                                                clientFired.remove(prj.getOwner());
                                        }
                                        deadPrj.clear();
                                }
                        }

The way to fix this is to add a remove_flag variable to the Projectile class, and raise the flag in the moveProjectile function. All the projectiles that are "dead" should be flagged.
Then in the above code, you just have to check if the current missile has already been destroyed and only call moveProjectile if it hasn't been flagged yet.

As follows:

                                while(it.hasNext()) {   
                                        Object o = it.next();
                                        assert(o instanceof Projectile);
                                        
                                        // if this projectile hasn't already  been destroyed by another
                                        if (((Projectile)o).remove_flag == false)
                                            deadPrj.addAll(moveProjectile((Projectile)o));
                                }


The modifications you need to make are in MazeImpl.java, specifically in the missileTick() function which is meant to replace the run() method.
So the bug fix needs to be in the missileTick() method.

Also, you might need to add a remove_flag variable in order to keep track of whether the projectile was removed or not.


 		 	   		  
