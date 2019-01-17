# Zimulator

Zbox-based generic simulation system

## Abstract

City transit systems often include some type of vehicle network which
is by design separated from general city traffic; this type of public
transit is referred to in this work as a metro system. Such a system
typically includes uniform behaviour of vehicles with simple dynamics,
and a decoupling from weather, street traffic, and other interference.
Not all properties of such a system can be easily observed; obtaining
information about these hidden degrees of freedom, and also answering
questions about how such a system might behave in given scenarios
requires the use of a simulator.

A simulation system with generic properties is described, applicable
to systems comprised of objects which move on well-defined
trajectories, and have certain containment properties. Interaction
degrees of freedom of the objects in such a system include capacities,
speeds, origin-destination logic and schedules.  The philosophy is
primarily that complex aggregate behaviour can be a consequence of the
dynamics of objects with simple interaction rules.  Supplementing
this, the framework includes the ability to interface with external
servers in order to model behaviour which cannot be replicated with the
fundamental objects available.

Use of this framework is illustrated by modelling, using
publicly-available data, the vehicles and passengers of the metro
system of a particular world city, providing a so-called digital-twin
capability. After calibration of such a model, properties of the metro
system such as platform and train occupancies can be investigated, and
the effect of incidents such as blocked tracks or system breakdowns
can be predicted. The system and associated tools are released as
open-source software.

## Documentation

The full manual is located in 01_Documentation/01_Manual.pdf

